package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.entity.MovieEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MovieMapper {

    @Select({
            "<script>",
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
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

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM movies",
            "<if test='keyword != null'>",
            "WHERE LOWER(name) LIKE CONCAT('%', LOWER(#{keyword}), '%')",
            "</if>",
            "</script>"
    })
    long countMovies(@Param("keyword") String keyword);

    @Select("SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl FROM movies WHERE id = #{id}")
    MovieEntity findById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
            "FROM movies",
            "WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<MovieEntity> findByIds(@Param("ids") List<Long> ids);

    @Select({
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
            "FROM movies",
            "WHERE name IS NOT NULL AND name <> ''",
            "ORDER BY",
            "CASE WHEN poster_url IS NULL OR poster_url = '' THEN 1 ELSE 0 END ASC,",
            "CASE WHEN published_year IS NULL THEN 1 ELSE 0 END ASC,",
            "published_year DESC, id ASC",
            "LIMIT #{limit}"
    })
    List<MovieEntity> findPopularMovies(@Param("limit") int limit);

    @Select({
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
            "FROM movies",
            "WHERE name IS NOT NULL AND name <> ''",
            "ORDER BY id ASC",
            "LIMIT #{limit}"
    })
    List<MovieEntity> findFallbackMovies(@Param("limit") int limit);

    @Select({
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
            "FROM movies",
            "WHERE id <> #{movieId}",
            "AND type IS NOT NULL AND type <> ''",
            "AND #{genres} IS NOT NULL",
            "AND LOWER(type) LIKE CONCAT('%', LOWER(#{genres}), '%')",
            "LIMIT #{limit}"
    })
    List<MovieEntity> findByGenresLike(@Param("movieId") Long movieId, @Param("genres") String genres,
            @Param("limit") int limit);

    @org.apache.ibatis.annotations.Update("UPDATE movies SET poster_url = #{posterUrl} WHERE id = #{id}")
    int updatePosterUrl(@Param("id") Long id, @Param("posterUrl") String posterUrl);
}
