package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.entity.RatingEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Select("SELECT COUNT(*) FROM movie_preferences WHERE userID = #{userId} AND movieID = #{movieId}")
    int countByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Update("UPDATE movie_preferences SET preference = #{preference}, timestamp = #{timestamp} WHERE userID = #{userId} AND movieID = #{movieId}")
    int updateRating(@Param("userId") Long userId,
            @Param("movieId") Long movieId,
            @Param("preference") Double preference,
            @Param("timestamp") Long timestamp);

    @Insert("INSERT INTO movie_preferences (userID, movieID, preference, timestamp) VALUES (#{userId}, #{movieId}, #{preference}, #{timestamp})")
    int insertRating(@Param("userId") Long userId,
            @Param("movieId") Long movieId,
            @Param("preference") Double preference,
            @Param("timestamp") Long timestamp);

    @Select("SELECT mp.userID AS userId, mp.movieID AS movieId, mp.preference, mp.timestamp FROM movie_preferences mp INNER JOIN (SELECT movieID, MAX(timestamp) AS maxTimestamp FROM movie_preferences WHERE userID = #{userId} GROUP BY movieID) latest ON mp.movieID = latest.movieID AND mp.timestamp = latest.maxTimestamp WHERE mp.userID = #{userId} ORDER BY mp.timestamp DESC")
    List<RatingEntity> findByUserId(@Param("userId") Long userId);

    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp FROM movie_preferences WHERE userID = #{userId} AND movieID = #{movieId} ORDER BY timestamp DESC LIMIT 1")
    RatingEntity findLatestByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);
}
