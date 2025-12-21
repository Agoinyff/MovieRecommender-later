package com.rcd.movierecommender.backend.service.recommender.slopeone;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import java.util.*;

/**
 * 自研 Slope One 推荐器（兼容你当前 Mahout 版本的 Recommender 接口签名）
 *
 * 说明：
 * - 模型构建交给 SlopeOneModel（职责分离）
 * - 推荐阶段使用小顶堆维护 TopN（效率更高）
 */
public class CustomSlopeOneRecommender extends AbstractRecommender {

    private final SlopeOneModel model;

    public CustomSlopeOneRecommender(DataModel dataModel) throws TasteException {
        super(dataModel);
        this.model = new SlopeOneModel(dataModel);
    }

    /**
     * 常用重载：默认不包含已评分物品
     */
    @Override
    public List<RecommendedItem> recommend(long userID, int howMany) throws TasteException {
        return recommend(userID, howMany, null, false);
    }

    /**
     * 常用重载：支持 rescorer，默认不包含已评分物品
     */
    @Override
    public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
        return recommend(userID, howMany, rescorer, false);
    }

    /**
     * 关键：你项目依赖的 Mahout 版本要求的 4 参数 recommend
     */
    @Override
    public List<RecommendedItem> recommend(long userID,
                                           int howMany,
                                           IDRescorer rescorer,
                                           boolean includeKnownItems) throws TasteException {

        if (howMany <= 0) {
            return Collections.emptyList();
        }

        DataModel dm = getDataModel();

        // 获取用户评分
        PreferenceArray userPrefs = dm.getPreferencesFromUser(userID);
        if (userPrefs == null || userPrefs.length() == 0) {
            throw new TasteException("用户不存在或无评分记录 userID=" + userID);
        }

        // 用户已评分物品集合（用于过滤）
        Set<Long> rated = new HashSet<>();
        for (int i = 0; i < userPrefs.length(); i++) {
            rated.add(userPrefs.getItemID(i));
        }

        // TopN 小顶堆
        PriorityQueue<RecommendedItem> topN =
                new PriorityQueue<>(Comparator.comparing(RecommendedItem::getValue));

        LongPrimitiveIterator itemIt = dm.getItemIDs();
        while (itemIt.hasNext()) {
            long itemID = itemIt.nextLong();

            if (!includeKnownItems && rated.contains(itemID)) {
                continue;
            }

            double est = estimateFromUserPreferences(userPrefs, itemID);
            if (Double.isNaN(est)) {
                continue;
            }

            float score = (float) est;

            // 过滤与重评分
            if (rescorer != null) {
                if (rescorer.isFiltered(itemID)) {
                    continue;
                }
                score = (float) rescorer.rescore(itemID, score);
            }

            RecommendedItem rec = new GenericRecommendedItem(itemID, score);

            if (topN.size() < howMany) {
                topN.add(rec);
            } else if (score > topN.peek().getValue()) {
                topN.poll();
                topN.add(rec);
            }
        }

        List<RecommendedItem> result = new ArrayList<>(topN);
        result.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
        return result;
    }

    /**
     * 预测评分：若用户已对 item 评分，则直接返回原评分；否则按 Slope One 预测
     */
    @Override
    public float estimatePreference(long userID, long itemID) throws TasteException {
        PreferenceArray userPrefs = getDataModel().getPreferencesFromUser(userID);
        if (userPrefs == null || userPrefs.length() == 0) {
            throw new TasteException("用户不存在或无评分记录 userID=" + userID);
        }

        for (int i = 0; i < userPrefs.length(); i++) {
            if (userPrefs.getItemID(i) == itemID) {
                return userPrefs.getValue(i);
            }
        }

        double est = estimateFromUserPreferences(userPrefs, itemID);
        if (Double.isNaN(est)) {
            throw new TasteException("无法预测评分：缺少共现数据 itemID=" + itemID + ", userID=" + userID);
        }
        return (float) est;
    }

    /**
     * 给定用户评分列表，预测用户对 targetItem 的评分
     *
     * r̂(u,i) = Σ_j (r(u,j) + avgDiff(i,j)) * count(i,j) / Σ_j count(i,j)
     */
    private double estimateFromUserPreferences(PreferenceArray userPrefs, long targetItem) {
        double weightedSum = 0.0;
        int totalCount = 0;

        for (int i = 0; i < userPrefs.length(); i++) {
            long ratedItem = userPrefs.getItemID(i);
            float ratedValue = userPrefs.getValue(i);

            DeviationStat stat = model.getDeviation(targetItem, ratedItem);
            if (stat == null || stat.getCount() == 0) {
                continue;
            }

            weightedSum += (ratedValue + stat.getAverage()) * stat.getCount();
            totalCount += stat.getCount();
        }

        if (totalCount == 0) {
            return Double.NaN;
        }
        return weightedSum / totalCount;
    }

    /**
     * refresh：按需重建模型（实验阶段可用；生产可改为定时重建/增量更新）
     */
    @Override
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
        try {
            model.rebuild(getDataModel());
        } catch (TasteException e) {
            // 失败时不抛出运行时异常，避免影响系统可用性；可按需记录日志
        }
    }
}
