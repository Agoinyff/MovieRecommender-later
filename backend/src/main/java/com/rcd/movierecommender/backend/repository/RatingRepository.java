package com.rcd.movierecommender.backend.repository;

import com.rcd.movierecommender.backend.entity.RatingEntity;
import com.rcd.movierecommender.backend.entity.RatingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface RatingRepository extends JpaRepository<RatingEntity, RatingId> {

    List<RatingEntity> findByUserId(Long userId);

    List<RatingEntity> findByMovieId(Long movieId);

    @Query("select distinct r.userId from RatingEntity r")
    Set<Long> findDistinctUserIds();

    /**
     * 流式查询所有评分数据，避免一次性加载到内存
     * 注意：必须在事务中使用，并且要及时关闭 Stream
     */
    @QueryHints(value = {
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "1000"),
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "false"),
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_READONLY, value = "true")
    })
    @Query("select r from RatingEntity r")
    Stream<RatingEntity> streamAll();

    /**
     * 分页查询评分数据
     */
    Page<RatingEntity> findAll(Pageable pageable);
    
    /**
     * 查询评分总数
     */
    @Query("select count(r) from RatingEntity r")
    long countAll();
}
