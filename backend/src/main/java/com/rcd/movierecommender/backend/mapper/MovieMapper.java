package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.entity.MovieEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
            "SELECT COUNT(*)",
            "FROM movies",
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
            "<script>",
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
            "FROM movies",
            "WHERE id != #{movieId}",
            "<if test='genres != null and genres.size() > 0'>",
            "AND (",
            "<foreach collection='genres' item='genre' separator=' OR '>",
            "LOWER(type) LIKE CONCAT('%', LOWER(#{genre}), '%')",
            "</foreach>",
            ")",
            "</if>",
            "ORDER BY id DESC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<MovieEntity> findRelatedMovies(@Param("movieId") Long movieId,
            @Param("genres") List<String> genres,
            @Param("limit") int limit);

    @Select("SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl FROM movies WHERE id != #{movieId} ORDER BY id DESC LIMIT #{limit}")
    List<MovieEntity> findRecentMoviesExcluding(@Param("movieId") Long movieId, @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
            "FROM movies",
            "WHERE 1 = 1",
            "<if test='excludedIds != null and excludedIds.size() > 0'>",
            "AND id NOT IN",
            "<foreach collection='excludedIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</if>",
            "<if test='genres != null and genres.size() > 0'>",
            "AND (",
            "<foreach collection='genres' item='genre' separator=' OR '>",
            "LOWER(type) LIKE CONCAT('%', LOWER(#{genre}), '%')",
            "</foreach>",
            ")",
            "</if>",
            "ORDER BY id DESC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<MovieEntity> findMoviesByGenresExcludingIds(@Param("genres") List<String> genres,
            @Param("excludedIds") List<Long> excludedIds,
            @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT id, name, published_year AS publishedYear, type AS genres, poster_url AS posterUrl",
            "FROM movies",
            "WHERE 1 = 1",
            "<if test='excludedIds != null and excludedIds.size() > 0'>",
            "AND id NOT IN",
            "<foreach collection='excludedIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</if>",
            "ORDER BY id DESC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<MovieEntity> findRecentMoviesExcludingIds(@Param("excludedIds") List<Long> excludedIds,
            @Param("limit") int limit);

    @Update("UPDATE movies SET poster_url = #{posterUrl} WHERE id = #{id}")
    int updatePosterUrl(@Param("id") Long id, @Param("posterUrl") String posterUrl);
}