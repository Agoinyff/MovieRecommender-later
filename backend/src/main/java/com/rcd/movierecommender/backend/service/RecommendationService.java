package com.rcd.movierecommender.backend.service;

import com.rcd.movierecommender.backend.dto.MovieDto;
import com.rcd.movierecommender.backend.dto.RecommendationDto;
import com.rcd.movierecommender.backend.dto.RecommendationStrategy;
import com.rcd.movierecommender.backend.entity.MovieEntity;
import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.repository.MovieRepository;
import com.rcd.movierecommender.backend.repository.RatingRepository;
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
        Map<Long, Map<Long, Double>> ratingsByUser = loadRatingsByUser();
        Map<Long, Double> targetRatings = ratingsByUser.getOrDefault(userId, Collections.emptyMap());
        if (targetRatings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Double> scored;
        switch (strategy) {
            case ITEM_BASED:
                scored = recommendItemBased(userId, ratingsByUser);
                break;
            case SLOPE_ONE:
                scored = recommendSlopeOne(userId, ratingsByUser);
                break;
            case USER_BASED:
            default:
                scored = recommendUserBased(userId, ratingsByUser);
                break;
        }

        return scored.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(size)
                .map(entry -> toRecommendationDto(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 从数据库加载所有评分，转换为 {userId -> {movieId -> rating}} 的嵌套 Map。
     */
    private Map<Long, Map<Long, Double>> loadRatingsByUser() {
        Map<Long, Map<Long, Double>> ratingsByUser = new HashMap<>();
        List<RatingEntity> allRatings = ratingRepository.findAll();
        for (RatingEntity rating : allRatings) {
            ratingsByUser
                    .computeIfAbsent(rating.getUserId(), id -> new HashMap<>())
                    .put(rating.getMovieId(), rating.getPreference().doubleValue());
        }
        return ratingsByUser;
    }

    /**
     * 用户协同过滤：计算目标用户与其他用户的余弦相似度，根据相似用户的评分为未看电影加权评分。
     */
    private Map<Long, Double> recommendUserBased(Long userId, Map<Long, Map<Long, Double>> ratingsByUser) {
        Map<Long, Double> targetRatings = ratingsByUser.getOrDefault(userId, Collections.emptyMap());
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, Double> similarityTotals = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : ratingsByUser.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) {
                continue;
            }
            double similarity = cosineSimilarity(targetRatings, entry.getValue());
            if (similarity <= 0) {
                continue;
            }
            for (Map.Entry<Long, Double> movieRating : entry.getValue().entrySet()) {
                Long movieId = movieRating.getKey();
                if (targetRatings.containsKey(movieId)) {
                    continue;
                }
                scores.merge(movieId, movieRating.getValue() * similarity, Double::sum);
                similarityTotals.merge(movieId, similarity, Double::sum);
            }
        }

        Map<Long, Double> results = new HashMap<>();
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            double totalSim = similarityTotals.getOrDefault(entry.getKey(), 1.0);
            results.put(entry.getKey(), entry.getValue() / totalSim);
        }
        return results;
    }

    /**
     * 物品协同过滤：先按电影重组评分矩阵，再用余弦相似度衡量电影间相似度，为目标用户未看过的电影计算加权得分。
     */
    private Map<Long, Double> recommendItemBased(Long userId, Map<Long, Map<Long, Double>> ratingsByUser) {
        Map<Long, Double> targetRatings = ratingsByUser.getOrDefault(userId, Collections.emptyMap());
        Map<Long, Map<Long, Double>> ratingsByMovie = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> userEntry : ratingsByUser.entrySet()) {
            for (Map.Entry<Long, Double> rating : userEntry.getValue().entrySet()) {
                ratingsByMovie
                        .computeIfAbsent(rating.getKey(), id -> new HashMap<>())
                        .put(userEntry.getKey(), rating.getValue());
            }
        }

        Map<Long, Double> scores = new HashMap<>();
        Map<Long, Double> weight = new HashMap<>();

        for (Map.Entry<Long, Double> targetRating : targetRatings.entrySet()) {
            Long targetMovie = targetRating.getKey();
            Map<Long, Double> targetMovieRatings = ratingsByMovie.getOrDefault(targetMovie, Collections.emptyMap());
            for (Map.Entry<Long, Map<Long, Double>> candidateEntry : ratingsByMovie.entrySet()) {
                Long candidateMovie = candidateEntry.getKey();
                if (candidateMovie.equals(targetMovie) || targetRatings.containsKey(candidateMovie)) {
                    continue;
                }
                double similarity = cosineSimilarity(targetMovieRatings, candidateEntry.getValue());
                if (similarity <= 0) {
                    continue;
                }
                scores.merge(candidateMovie, similarity * targetRating.getValue(), Double::sum);
                weight.merge(candidateMovie, similarity, Double::sum);
            }
        }

        Map<Long, Double> results = new HashMap<>();
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            double sim = weight.getOrDefault(entry.getKey(), 1.0);
            results.put(entry.getKey(), entry.getValue() / sim);
        }
        return results;
    }

    /**
     * Slope One：利用评分差矩阵（diff）和频次矩阵（freq）对未看电影进行差值预测。
     */
    private Map<Long, Double> recommendSlopeOne(Long userId, Map<Long, Map<Long, Double>> ratingsByUser) {
        Map<Long, Double> targetRatings = ratingsByUser.getOrDefault(userId, Collections.emptyMap());
        Set<Long> targetMovies = targetRatings.keySet();

        Map<Long, Map<Long, Double>> diff = new HashMap<>();
        Map<Long, Map<Long, Integer>> freq = new HashMap<>();

        for (Map<Long, Double> userRatings : ratingsByUser.values()) {
            for (Map.Entry<Long, Double> entryA : userRatings.entrySet()) {
                Long itemA = entryA.getKey();
                if (!targetMovies.contains(itemA)) {
                    continue;
                }
                for (Map.Entry<Long, Double> entryB : userRatings.entrySet()) {
                    Long itemB = entryB.getKey();
                    if (itemA.equals(itemB)) {
                        continue;
                    }
                    double difference = entryA.getValue() - entryB.getValue();
                    diff.computeIfAbsent(itemA, key -> new HashMap<>())
                            .merge(itemB, difference, Double::sum);
                    freq.computeIfAbsent(itemA, key -> new HashMap<>())
                            .merge(itemB, 1, Integer::sum);
                }
            }
        }

        Map<Long, Double> predictions = new HashMap<>();
        Map<Long, Integer> frequencies = new HashMap<>();

        for (Map.Entry<Long, Double> targetEntry : targetRatings.entrySet()) {
            Long targetMovie = targetEntry.getKey();
            double targetRating = targetEntry.getValue();
            Map<Long, Double> movieDiffs = diff.getOrDefault(targetMovie, Collections.emptyMap());
            Map<Long, Integer> movieFreqs = freq.getOrDefault(targetMovie, Collections.emptyMap());

            for (Map.Entry<Long, Double> diffEntry : movieDiffs.entrySet()) {
                Long relatedMovie = diffEntry.getKey();
                if (targetMovies.contains(relatedMovie)) {
                    continue;
                }
                double averageDiff = diffEntry.getValue() / movieFreqs.getOrDefault(relatedMovie, 1);
                double predictedRating = targetRating + averageDiff;
                predictions.merge(relatedMovie, predictedRating * movieFreqs.getOrDefault(relatedMovie, 1), Double::sum);
                frequencies.merge(relatedMovie, movieFreqs.getOrDefault(relatedMovie, 1), Integer::sum);
            }
        }

        Map<Long, Double> results = new HashMap<>();
        for (Map.Entry<Long, Double> entry : predictions.entrySet()) {
            int count = frequencies.getOrDefault(entry.getKey(), 1);
            results.put(entry.getKey(), entry.getValue() / count);
        }
        return results;
    }

    /**
     * 基于共有评分的余弦相似度计算。
     */
    private double cosineSimilarity(Map<Long, Double> ratingsA, Map<Long, Double> ratingsB) {
        Set<Long> sharedMovies = new HashSet<>(ratingsA.keySet());
        sharedMovies.retainAll(ratingsB.keySet());
        if (sharedMovies.isEmpty()) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (Long movieId : sharedMovies) {
            double ratingA = ratingsA.get(movieId);
            double ratingB = ratingsB.get(movieId);
            dotProduct += ratingA * ratingB;
        }
        for (double rating : ratingsA.values()) {
            normA += rating * rating;
        }
        for (double rating : ratingsB.values()) {
            normB += rating * rating;
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 将电影 ID 与得分封装为传输对象；若电影不存在则返回 null 以便上游过滤。
     */
    private RecommendationDto toRecommendationDto(Long movieId, double score) {
        Optional<MovieEntity> movieOpt = movieRepository.findById(movieId);
        if (!movieOpt.isPresent()) {
            return null;
        }
        MovieEntity movie = movieOpt.get();
        return new RecommendationDto(movie.getId(), movie.getName(), movie.getPublishedYear(), movie.getGenres(), score);
    }
}
