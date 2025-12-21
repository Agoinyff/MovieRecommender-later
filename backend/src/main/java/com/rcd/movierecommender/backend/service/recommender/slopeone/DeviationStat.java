package com.rcd.movierecommender.backend.service.recommender.slopeone;

/**
 * 差值统计结构：用于存储两个物品之间的偏差累计和与共现次数
 *
 * diffSum = Σ(r_i - r_j)
 * count   = 共同被同一用户评分的次数
 *
 * avgDiff = diffSum / count
 */
public final class DeviationStat {

    /** 差值累加和 Σ(r_i - r_j) */
    private double diffSum = 0.0;

    /** 共现次数 */
    private int count = 0;

    /**
     * 添加一次差值样本
     * @param diff r_i - r_j
     */
    public void add(double diff) {
        this.diffSum += diff;
        this.count += 1;
    }

    /**
     * 返回平均差值 avgDiff
     */
    public double getAverage() {
        return count == 0 ? Double.NaN : diffSum / count;
    }

    /**
     * 返回共现次数
     */
    public int getCount() {
        return count;
    }
}
