package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.entity.MovieEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MovieMapper {

    /**
     * 分页检索电影（可选关键词）。
     * 这里直接用 SQL 拼接，便于与论文中“简单直观”的实现对应。
     */
    @Select({
            "<script>",
            "SELECT id, name, published_year AS publishedYear, type AS genres",
            "FROM movies",
            "<if test='keyword != null'>",
            "WHERE LOWER(name) LIKE CONCAT('%', LOWER(#{keyword}), '%')",
            "</if>",
            "ORDER BY id",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<MovieEntity> findMovies(@Param("keyword") String keyword,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    /**
     * 统计电影总数（可选关键词）。
     */
    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM movies",
            "<if test='keyword != null'>",
            "WHERE LOWER(name) LIKE CONCAT('%', LOWER(#{keyword}), '%')",
            "</if>",
            "</script>"
    })
    long countMovies(@Param("keyword") String keyword);

    /**
     * 按主键查询电影详情。
     */
    @Select("SELECT id, name, published_year AS publishedYear, type AS genres FROM movies WHERE id = #{id}")
    MovieEntity findById(@Param("id") Long id);

    /**
     * 批量查询电影详情，用于推荐结果回填。
     */
    @Select({
            "<script>",
            "SELECT id, name, published_year AS publishedYear, type AS genres",
            "FROM movies",
            "WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<MovieEntity> findByIds(@Param("ids") List<Long> ids);
}
