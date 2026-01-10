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
}
