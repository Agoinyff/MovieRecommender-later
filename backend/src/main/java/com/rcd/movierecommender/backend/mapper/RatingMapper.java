package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.dto.RatingDto;
import com.rcd.movierecommender.backend.entity.RatingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RatingMapper {

    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp FROM movie_preferences")
    List<RatingEntity> findAllRatings();

    @Select("SELECT COUNT(*) FROM movie_preferences")
    long countRatings();

    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp FROM movie_preferences LIMIT #{limit} OFFSET #{offset}")
    List<RatingEntity> findRatingsByPage(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp FROM movie_preferences")
    @ResultType(RatingEntity.class)
    @org.apache.ibatis.annotations.Options(fetchSize = 10000)
    void streamAllRatings(org.apache.ibatis.session.ResultHandler<RatingEntity> handler);

    @org.apache.ibatis.annotations.Insert("INSERT INTO movie_preferences (userID, movieID, preference, timestamp) VALUES (#{userId}, #{movieId}, #{preference}, #{timestamp}) ON DUPLICATE KEY UPDATE preference = #{preference}, timestamp = #{timestamp}")
    void insertOrUpdate(@Param("userId") Long userId,
            @Param("movieId") Long movieId,
            @Param("preference") Double preference,
            @Param("timestamp") Long timestamp);

    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp FROM movie_preferences WHERE userID = #{userId} ORDER BY timestamp DESC")
    List<RatingEntity> findByUserId(@Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT mp.userID AS userId, mp.movieID AS movieId, m.name AS movieName, mp.preference AS rating, mp.timestamp AS timestamp",
            "FROM movie_preferences mp",
            "JOIN movies m ON m.id = mp.movieID",
            "WHERE mp.userID = #{userId}",
            "<if test='query != null and query != \"\"'>",
            "AND LOWER(m.name) LIKE CONCAT('%', LOWER(#{query}), '%')",
            "</if>",
            "<if test='minRating != null'>",
            "AND mp.preference &gt;= #{minRating}",
            "</if>",
            "<if test='maxRating != null'>",
            "AND mp.preference &lt;= #{maxRating}",
            "</if>",
            "ORDER BY mp.timestamp DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<RatingDto> findFilteredUserRatings(@Param("userId") Long userId,
            @Param("query") String query,
            @Param("minRating") Double minRating,
            @Param("maxRating") Double maxRating,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM movie_preferences mp",
            "JOIN movies m ON m.id = mp.movieID",
            "WHERE mp.userID = #{userId}",
            "<if test='query != null and query != \"\"'>",
            "AND LOWER(m.name) LIKE CONCAT('%', LOWER(#{query}), '%')",
            "</if>",
            "<if test='minRating != null'>",
            "AND mp.preference &gt;= #{minRating}",
            "</if>",
            "<if test='maxRating != null'>",
            "AND mp.preference &lt;= #{maxRating}",
            "</if>",
            "</script>"
    })
    long countFilteredUserRatings(@Param("userId") Long userId,
            @Param("query") String query,
            @Param("minRating") Double minRating,
            @Param("maxRating") Double maxRating);

    @Select("SELECT userID AS userId, movieID AS movieId, preference AS rating, timestamp FROM movie_preferences WHERE userID = #{userId} AND movieID = #{movieId} LIMIT 1")
    RatingDto findUserMovieRating(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Select("SELECT COUNT(*) FROM movie_preferences WHERE userID = #{userId}")
    long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COALESCE(AVG(preference), 0) FROM movie_preferences WHERE userID = #{userId}")
    Double getAverageRatingByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM movie_preferences WHERE userID = #{userId} AND preference >= 4")
    long countHighRatingsByUserId(@Param("userId") Long userId);

    @Select("SELECT MAX(timestamp) FROM movie_preferences WHERE userID = #{userId}")
    Long getRecentTimestampByUserId(@Param("userId") Long userId);
}
