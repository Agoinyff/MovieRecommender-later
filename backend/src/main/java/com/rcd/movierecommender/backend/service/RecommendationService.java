package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.repository.MovieRepository;
import com.rcd.movierecommender.backend.repository.RatingRepository;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;

    /**
     * 推荐服务核心：围绕用户评分构建三种策略（用户协同、物品协同、Slope One）。
     * 所有方法均在只读事务下执行，避免误写数据库。
     */
    public RecommendationService(RatingRepository ratingRepository, MovieRepository movieRepository) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
    }

    /**
     * 为指定用户生成推荐结果。
     *
     * <p>流程：</p>
     * <ol>
     *     <li>从数据库读取所有评分，构建用户-电影评分矩阵。</li>
     *     <li>根据策略选择对应的打分算法，得到候选电影的预测得分。</li>
     *     <li>按得分降序截断 size 条，并补充电影元数据。</li>
     * </ol>
     *
     * @param userId   目标用户 ID。
     * @param size     返回条数。
     * @param strategy 推荐策略（用户、物品、Slope One）。
     * @return 推荐结果列表。
     */
    public List<RecommendationDto> recommend(Long userId, int size, RecommendationStrategy strategy) {
        DataModel dataModel = buildDataModel();
        if (!userExists(userId, dataModel)) {
            return Collections.emptyList();
        }

        try {
            List<RecommendedItem> recommendedItems = buildRecommender(dataModel, strategy)
                    .recommend(userId, size);
            return recommendedItems.stream()
                    .map(item -> toRecommendationDto(item.getItemID(), item.getValue()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (TasteException e) {
            throw new IllegalStateException("Failed to generate recommendations with Mahout", e);
        }
    }

    private DataModel buildDataModel() {
        FastByIDMap<PreferenceArray> preferenceMap = new FastByIDMap<>();
        Map<Long, List<Preference>> preferences = new HashMap<>();
        for (RatingEntity rating : ratingRepository.findAll()) {
            preferences
                    .computeIfAbsent(rating.getUserId(), id -> new ArrayList<>())
                    .add(new GenericPreference(rating.getUserId(), rating.getMovieId(), rating.getPreference().floatValue()));
        }
        for (Map.Entry<Long, List<Preference>> entry : preferences.entrySet()) {
            preferenceMap.put(entry.getKey(), new GenericUserPreferenceArray(entry.getValue()));
        }
        return new GenericDataModel(preferenceMap);
    }

    private boolean userExists(Long userId, DataModel dataModel) {
        try {
            dataModel.getPreferencesFromUser(userId);
            return true;
        } catch (TasteException e) {
            return false;
        }
    }

    private Recommender buildRecommender(DataModel dataModel, RecommendationStrategy strategy) throws TasteException {
        switch (strategy) {
            case ITEM_BASED:
                ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(dataModel);
                return new GenericItemBasedRecommender(dataModel, itemSimilarity);
            case SLOPE_ONE:
                return new SlopeOneRecommender(dataModel);
            case USER_BASED:
            default:
                UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(dataModel);
                UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, userSimilarity, dataModel);
                return new GenericUserBasedRecommender(dataModel, neighborhood, userSimilarity);
        }
    }

    private RecommendationDto toRecommendationDto(Long movieId, double score) {
        Optional<MovieEntity> movieOpt = movieRepository.findById(movieId);
        if (!movieOpt.isPresent()) {
            return null;
        }
        MovieEntity movie = movieOpt.get();
        return new RecommendationDto(movie.getId(), movie.getName(), movie.getPublishedYear(), movie.getGenres(), score);
    }
}
