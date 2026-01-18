package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.entity.RatingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RatingMapper {

    /**
     * 查询所有评分数据，用于构建 Mahout DataModel。
     */
    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp FROM movie_preferences")
    List<RatingEntity> findAllRatings();

    /**
     * 查询评分总数（用于健康检查接口）。
     */
    @Select("SELECT COUNT(*) FROM movie_preferences")
    long countRatings();

    /**
     * 分页查询评分数据，用于批量加载避免 OOM。
     * 
     * @param offset 偏移量
     * @param limit  每批数量
     * @return 评分数据列表
     */
    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp " +
            "FROM movie_preferences " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<RatingEntity> findRatingsByPage(@org.apache.ibatis.annotations.Param("offset") int offset,
            @org.apache.ibatis.annotations.Param("limit") int limit);

    /**
     * 统计评分总数（用于分页加载）。
     */
    @Select("SELECT COUNT(*) FROM movie_preferences")
    long countAllRatings();

    /**
     * 插入或更新用户评分
     * 
     * @param userId     用户 ID
     * @param movieId    电影 ID
     * @param preference 评分
     * @param timestamp  时间戳
     */
    @org.apache.ibatis.annotations.Insert("INSERT INTO movie_preferences (userID, movieID, preference, timestamp) " +
            "VALUES (#{userId}, #{movieId}, #{preference}, #{timestamp}) " +
            "ON DUPLICATE KEY UPDATE preference = #{preference}, timestamp = #{timestamp}")
    void insertOrUpdate(@org.apache.ibatis.annotations.Param("userId") Long userId,
            @org.apache.ibatis.annotations.Param("movieId") Long movieId,
            @org.apache.ibatis.annotations.Param("preference") Double preference,
            @org.apache.ibatis.annotations.Param("timestamp") Long timestamp);

    /**
     * 查询指定用户的所有评分
     * 
     * @param userId 用户 ID
     * @return 评分列表
     */
    @Select("SELECT userID AS userId, movieID AS movieId, preference, timestamp " +
            "FROM movie_preferences WHERE userID = #{userId} ORDER BY timestamp DESC")
    List<RatingEntity> findByUserId(@org.apache.ibatis.annotations.Param("userId") Long userId);
}
