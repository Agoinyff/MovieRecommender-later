#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
离线评估脚本：对比 USER_BASED / ITEM_BASED / SLOPE_ONE 三种策略在同一数据切分下的 Recall@K / F1@K / Coverage@K。

能力：
1) 自动切分 SQL（按用户时间倒序，留最后 N 条作为测试集）
2) 自动备份/恢复 movie_preferences
3) 调用后端接口重建模型并拉取推荐结果
4) 导出 summary + per_user 两份 CSV 报告

注意：
- 需要 MySQL 8+（使用 ROW_NUMBER 窗口函数）
- 需要后端服务已启动，并可访问 /api/model/status /api/model/rebuild /api/recommendations
"""

from __future__ import annotations

import argparse
import csv
import datetime as dt
import json
import sys
import time
import traceback
import random
from collections import defaultdict
from dataclasses import dataclass
from typing import Dict, List, Set, Tuple


pymysql = None
requests = None

STRATEGIES = ["USER_BASED", "ITEM_BASED", "SLOPE_ONE"]
# STRATEGIES = ["USER_BASED"]



@dataclass
class EvalConfig:
    db_host: str
    db_port: int
    db_user: str
    db_password: str
    db_name: str
    api_base: str
    k: int
    min_ratings: int
    holdout_per_user: int
    model_timeout_s: int
    request_timeout_s: int
    poll_interval_s: int
    output_dir: str


class Evaluator:
    def __init__(self, cfg: EvalConfig):
        self.cfg = cfg
        self.conn = None
        self.cursor = None
        self.backup_table = None
        self.eval_ranked_table = "eval_ranked"
        self.eval_train_table = "eval_train"
        self.eval_test_table = "eval_test"

    def connect_db(self):
        self.conn = pymysql.connect(
            host=self.cfg.db_host,
            port=self.cfg.db_port,
            user=self.cfg.db_user,
            password=self.cfg.db_password,
            database=self.cfg.db_name,
            charset="utf8mb4",
            autocommit=True,
            cursorclass=pymysql.cursors.DictCursor,
        )
        self.cursor = self.conn.cursor()

    def close_db(self):
        if self.cursor:
            self.cursor.close()
        if self.conn:
            self.conn.close()

    def _exec(self, sql: str):
        self.cursor.execute(sql)

    def _query_all(self, sql: str) -> List[Dict]:
        self.cursor.execute(sql)
        return list(self.cursor.fetchall())

    def _query_one(self, sql: str) -> Dict:
        self.cursor.execute(sql)
        row = self.cursor.fetchone()
        return row or {}

    def verify_tables(self):
        must_tables = ["movie_preferences", "movies"]
        for t in must_tables:
            row = self._query_one(f"SHOW TABLES LIKE '{t}'")
            if not row:
                raise RuntimeError(f"缺少必要数据表: {t}")

    def backup_movie_preferences(self):
        ts = dt.datetime.now().strftime("%Y%m%d_%H%M%S")
        self.backup_table = f"movie_preferences_eval_backup_{ts}"
        print(f"[INFO] 备份 movie_preferences -> {self.backup_table}")
        self._exec(f"CREATE TABLE `{self.backup_table}` LIKE `movie_preferences`")
        self._exec(f"INSERT INTO `{self.backup_table}` SELECT * FROM `movie_preferences`")

    def restore_movie_preferences(self):
        if not self.backup_table:
            return
        print(f"[INFO] 恢复 movie_preferences <- {self.backup_table}")
        self._exec("TRUNCATE TABLE `movie_preferences`")
        self._exec(f"INSERT INTO `movie_preferences` SELECT * FROM `{self.backup_table}`")

    def cleanup_temp_tables(self):
        for t in [self.eval_ranked_table, self.eval_train_table, self.eval_test_table]:
            self._exec(f"DROP TABLE IF EXISTS `{t}`")

    def split_train_test(self):
        print("[INFO] 生成评估切分（train/test）...")
        self.cleanup_temp_tables()

        self._exec(
            f"""
            CREATE TABLE `{self.eval_ranked_table}` AS
            SELECT
                userID,
                movieID,
                preference,
                `timestamp`,
                ROW_NUMBER() OVER(PARTITION BY userID ORDER BY `timestamp` DESC, movieID DESC) AS rn,
                COUNT(*) OVER(PARTITION BY userID) AS cnt
            FROM movie_preferences
            """
        )

        holdout = int(self.cfg.holdout_per_user)
        min_r = int(self.cfg.min_ratings)

        self._exec(
            f"""
            CREATE TABLE `{self.eval_test_table}` AS
            SELECT userID, movieID, preference, `timestamp`
            FROM `{self.eval_ranked_table}`
            WHERE rn <= {holdout} AND cnt >= {min_r}
            """
        )

        self._exec(
            f"""
            CREATE TABLE `{self.eval_train_table}` AS
            SELECT userID, movieID, preference, `timestamp`
            FROM `{self.eval_ranked_table}`
            WHERE rn > {holdout} AND cnt >= {min_r}
            """
        )

        c_train = self._query_one(f"SELECT COUNT(*) AS c FROM `{self.eval_train_table}`").get("c", 0)
        c_test = self._query_one(f"SELECT COUNT(*) AS c FROM `{self.eval_test_table}`").get("c", 0)
        c_users = self._query_one(f"SELECT COUNT(DISTINCT userID) AS c FROM `{self.eval_test_table}`").get("c", 0)

        print(f"[INFO] 切分完成: train={c_train}, test={c_test}, eval_users={c_users}")
        if c_users == 0:
            raise RuntimeError("评估用户数为 0，请降低 min-ratings 或检查数据")

    def replace_with_train_set(self):
        print("[INFO] 使用训练集覆盖 movie_preferences（评估模式）")
        self._exec("TRUNCATE TABLE `movie_preferences`")
        self._exec(
            f"INSERT INTO `movie_preferences` (userID, movieID, preference, `timestamp`) "
            f"SELECT userID, movieID, preference, `timestamp` FROM `{self.eval_train_table}`"
        )

    def get_test_truth(self) -> Dict[int, Set[int]]:
        rows = self._query_all(
        f"SELECT userID, movieID FROM `{self.eval_test_table}` WHERE preference >= 3"
    )
        truth: Dict[int, Set[int]] = defaultdict(set)
        for r in rows:
            truth[int(r["userID"])].add(int(r["movieID"]))
        return truth

    def get_catalog_size(self) -> int:
        return int(self._query_one("SELECT COUNT(*) AS c FROM movies").get("c", 0))

    # ---------- API ----------

    def _api_get(self, path: str, params: Dict | None = None) -> Dict | List:
        url = self.cfg.api_base.rstrip("/") + path
        resp = requests.get(url, params=params, timeout=self.cfg.request_timeout_s)
        resp.raise_for_status()
        return resp.json()

    def _api_post(self, path: str) -> Dict | str:
        url = self.cfg.api_base.rstrip("/") + path
        resp = requests.post(url, timeout=self.cfg.request_timeout_s)
        resp.raise_for_status()
        try:
            return resp.json()
        except Exception:
            return resp.text

    def rebuild_and_wait(self):
        print("[INFO] 触发模型重建")
        self._api_post("/model/rebuild")

        start = time.time()
        while True:
            status = self._api_get("/model/status")
            build_status = str(status.get("buildStatus", "")).upper()
            if build_status == "SUCCESS":
                print("[INFO] 模型重建成功")
                return
            if build_status == "FAILED":
                raise RuntimeError(f"模型重建失败: {json.dumps(status, ensure_ascii=False)}")

            if time.time() - start > self.cfg.model_timeout_s:
                raise TimeoutError(f"等待模型重建超时（>{self.cfg.model_timeout_s}s）")
            time.sleep(self.cfg.poll_interval_s)

    def fetch_recommendations(self, user_id: int, strategy: str) -> List[int]:
        data = self._api_get(
            "/recommendations",
            params={
                "userId": user_id,
                "size": self.cfg.k,
                "strategy": strategy,
            },
        )
        movie_ids = []
        for item in data:
            mid = item.get("movieId")
            if mid is not None:
                movie_ids.append(int(mid))
        return movie_ids

    # ---------- metrics ----------

    def evaluate_strategy(
        self,
        strategy: str,
        truth_map: Dict[int, Set[int]],
        catalog_size: int,
    ) -> Tuple[Dict, List[Dict]]:
        all_rec_items: Set[int] = set()
        per_user_rows: List[Dict] = []

        recalls = []
        precisions = []
        f1s = []

        users = sorted(truth_map.keys())
        random.seed(42)  # 固定随机种子，保证每次抽到同一批用户（论文更好写）
        users = random.sample(users, k=min(200, len(users)))
        print(f"[INFO] 评估策略 {strategy}，用户数={len(users)}")

        for idx, uid in enumerate(users, start=1):
            truth = truth_map[uid]
            pred = self.fetch_recommendations(uid, strategy)
            pred_set = set(pred)
            all_rec_items.update(pred_set)

            hit = len(pred_set & truth)
            precision = hit / float(self.cfg.k) if self.cfg.k else 0.0
            recall = hit / float(len(truth)) if truth else 0.0
            f1 = (2 * precision * recall / (precision + recall)) if (precision + recall) > 0 else 0.0

            recalls.append(recall)
            precisions.append(precision)
            f1s.append(f1)

            per_user_rows.append(
                {
                    "strategy": strategy,
                    "user_id": uid,
                    "truth_count": len(truth),
                    "pred_count": len(pred),
                    "hit_count": hit,
                    "precision_at_k": round(precision, 8),
                    "recall_at_k": round(recall, 8),
                    "f1_at_k": round(f1, 8),
                    "truth_movie_ids": "|".join(map(str, sorted(truth))),
                    "pred_movie_ids": "|".join(map(str, pred)),
                }
            )

            if idx % 100 == 0:
                print(f"[INFO] {strategy} 进度: {idx}/{len(users)}")

        summary = {
            "strategy": strategy,
            "users": len(users),
            "k": self.cfg.k,
            "precision_at_k": round(sum(precisions) / len(precisions), 8) if precisions else 0.0,
            "recall_at_k": round(sum(recalls) / len(recalls), 8) if recalls else 0.0,
            "f1_at_k": round(sum(f1s) / len(f1s), 8) if f1s else 0.0,
            "coverage_at_k": round(len(all_rec_items) / float(catalog_size), 8) if catalog_size else 0.0,
            "distinct_recommended_items": len(all_rec_items),
            "catalog_size": catalog_size,
        }

        return summary, per_user_rows

    def write_csv(self, summary_rows: List[Dict], user_rows: List[Dict]):
        ts = dt.datetime.now().strftime("%Y%m%d_%H%M%S")
        summary_path = f"{self.cfg.output_dir.rstrip('/')}/eval_summary_{ts}.csv"
        user_path = f"{self.cfg.output_dir.rstrip('/')}/eval_per_user_{ts}.csv"

        with open(summary_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=list(summary_rows[0].keys()))
            writer.writeheader()
            writer.writerows(summary_rows)

        with open(user_path, "w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=list(user_rows[0].keys()))
            writer.writeheader()
            writer.writerows(user_rows)

        print(f"[INFO] 已导出 summary CSV: {summary_path}")
        print(f"[INFO] 已导出 per-user CSV: {user_path}")
        return summary_path, user_path

    def run(self):
        summary_rows: List[Dict] = []
        user_rows: List[Dict] = []

        self.connect_db()
        try:
            self.verify_tables()
            self.backup_movie_preferences()
            self.split_train_test()
            self.replace_with_train_set()

            # 确保评估时模型基于 train 数据集
            self.rebuild_and_wait()

            truth_map = self.get_test_truth()
            catalog_size = self.get_catalog_size()
            if catalog_size <= 0:
                raise RuntimeError("movies 表为空，无法计算 Coverage")

            for strategy in STRATEGIES:
                summary, per_user = self.evaluate_strategy(strategy, truth_map, catalog_size)
                summary_rows.append(summary)
                user_rows.extend(per_user)

            if not summary_rows or not user_rows:
                raise RuntimeError("评估结果为空")

            summary_path, user_path = self.write_csv(summary_rows, user_rows)

            print("\n====== 评估汇总（Recall/F1/Coverage）======")
            for row in summary_rows:
                print(json.dumps(row, ensure_ascii=False))

            print("\n[INFO] 你要的相关数据已生成：")
            print(f"- 汇总: {summary_path}")
            print(f"- 明细: {user_path}")

        finally:
            # 无论成功失败，都尽量恢复数据
            try:
                self.restore_movie_preferences()
                self.rebuild_and_wait()
            except Exception as restore_err:
                print(f"[WARN] 恢复流程出现异常: {restore_err}", file=sys.stderr)
            try:
                self.cleanup_temp_tables()
            except Exception:
                pass
            self.close_db()


def parse_args() -> EvalConfig:
    p = argparse.ArgumentParser(description="MovieRecommender 三策略离线评估脚本")
    p.add_argument("--db-host", default="127.0.0.1")
    p.add_argument("--db-port", type=int, default=3306)
    p.add_argument("--db-user", required=True)
    p.add_argument("--db-password", required=True)
    p.add_argument("--db-name", default="movie")
    p.add_argument("--api-base", default="http://127.0.0.1:8080/api")
    p.add_argument("-k", type=int, default=10, help="Top-K")
    p.add_argument("--min-ratings", type=int, default=5, help="最小评分数（低于该值的用户不参与评估）")
    p.add_argument("--holdout-per-user", type=int, default=1, help="每个用户留作测试的最后 N 条")
    p.add_argument("--model-timeout-s", type=int, default=900, help="等待模型重建超时秒数")
    p.add_argument("--request-timeout-s", type=int, default=30, help="接口请求超时秒数")
    p.add_argument("--poll-interval-s", type=int, default=3, help="模型状态轮询间隔秒数")
    p.add_argument("--output-dir", default="./reports", help="CSV 输出目录")

    args = p.parse_args()

    if args.k <= 0:
        raise ValueError("k 必须 > 0")
    if args.k > 50:
        raise ValueError("k 不能 > 50（项目接口限制 size <= 50）")

    return EvalConfig(
        db_host=args.db_host,
        db_port=args.db_port,
        db_user=args.db_user,
        db_password=args.db_password,
        db_name=args.db_name,
        api_base=args.api_base,
        k=args.k,
        min_ratings=args.min_ratings,
        holdout_per_user=args.holdout_per_user,
        model_timeout_s=args.model_timeout_s,
        request_timeout_s=args.request_timeout_s,
        poll_interval_s=args.poll_interval_s,
        output_dir=args.output_dir,
    )


def ensure_output_dir(path: str):
    import os

    os.makedirs(path, exist_ok=True)


def ensure_dependencies():
    global pymysql, requests
    try:
        import pymysql as _pymysql  # type: ignore
        pymysql = _pymysql
    except ImportError:
        print("[ERROR] 缺少依赖 pymysql，请先安装：pip install pymysql", file=sys.stderr)
        sys.exit(2)

    try:
        import requests as _requests  # type: ignore
        requests = _requests
    except ImportError:
        print("[ERROR] 缺少依赖 requests，请先安装：pip install requests", file=sys.stderr)
        sys.exit(2)


def main():
    try:
        cfg = parse_args()
        ensure_dependencies()
        ensure_output_dir(cfg.output_dir)
        evaluator = Evaluator(cfg)
        evaluator.run()
    except Exception as e:
        print("[ERROR] 评估失败:", e, file=sys.stderr)
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
