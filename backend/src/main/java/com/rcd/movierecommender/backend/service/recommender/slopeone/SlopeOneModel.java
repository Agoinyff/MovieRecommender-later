package com.rcd.movierecommender.backend.service.recommender.slopeone;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/**
 * Slope One 模型：负责构建与维护“物品-物品”差值矩阵 deviations
 *
 * deviations[itemA][itemB] = (diffSum, count)
 * diffSum = Σ(rA - rB), count = 共现次数
 *
 * 特点：
 * - 稀疏存储：只保存发生过共现评分的 item 对
 * - 构建复杂度：Σ_u O(k_u^2)，k_u 为用户 u 的评分数
 */
public final class SlopeOneModel {

    /**
     * 稀疏差值矩阵
     * 外层 key：itemA
     * 内层 key：itemB
     * 值：DeviationStat(diffSum, count)
     */
    private final FastByIDMap<FastByIDMap<DeviationStat>> deviations = new FastByIDMap<>();

    public SlopeOneModel(DataModel dataModel) throws TasteException {
        rebuild(dataModel);
    }

    /**
     * 全量重建差值矩阵（适用于实验/离线阶段）
     */
    public void rebuild(DataModel dataModel) throws TasteException {
        deviations.clear();

        LongPrimitiveIterator userIt = dataModel.getUserIDs();
        while (userIt.hasNext()) {
            long userId = userIt.nextLong();
            PreferenceArray prefs = dataModel.getPreferencesFromUser(userId);
            int size = prefs.length();

            // 对同一用户的评分列表做两两组合，更新差值统计
            for (int a = 0; a < size; a++) {
                long itemA = prefs.getItemID(a);
                float valueA = prefs.getValue(a);

                FastByIDMap<DeviationStat> rowA = deviations.get(itemA);
                if (rowA == null) {
                    rowA = new FastByIDMap<>();
                    deviations.put(itemA, rowA);
                }

                for (int b = 0; b < size; b++) {
                    if (a == b) continue;

                    long itemB = prefs.getItemID(b);
                    float valueB = prefs.getValue(b);

                    DeviationStat stat = rowA.get(itemB);
                    if (stat == null) {
                        stat = new DeviationStat();
                        rowA.put(itemB, stat);
                    }
                    stat.add(valueA - valueB); // diff = rA - rB
                }
            }
        }
    }

    /**
     * 获取 targetItem 对 ratedItem 的差值统计（可能为 null）
     */
    public DeviationStat getDeviation(long targetItem, long ratedItem) {
        FastByIDMap<DeviationStat> row = deviations.get(targetItem);
        if (row == null) {
            return null;
        }
        return row.get(ratedItem);
    }

    /**
     * 若你需要做内存统计/调试，可暴露矩阵引用（可选）
     */
    public FastByIDMap<FastByIDMap<DeviationStat>> getDeviations() {
        return deviations;
    }
}
