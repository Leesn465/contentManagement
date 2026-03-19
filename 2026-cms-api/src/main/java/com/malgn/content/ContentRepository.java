package com.malgn.content;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 콘텐츠 Repository
 * - N+1 문제 방지를 위해 author를 fetch join으로 조회
 * - 커서 기반 페이징을 위한 쿼리 제공
 */
@Repository
public interface ContentRepository extends JpaRepository<Content,Long> {
    // Page<Content> findAllByOrderByCreatedDateDesc(Pageable pageable);

    /**
     * 첫 페이지 조회
     * - 최신순 정렬 (createdDate desc, id desc)
     * - 인덱스를 활용한 빠른 조회
     */
    @Query("""
    SELECT c FROM Content c
    JOIN FETCH c.author
    ORDER BY c.createdDate DESC, c.id DESC
""")
    List<Content> findFirstPage(Pageable pageable);


    /**
     * 커서 기반 다음 페이지 조회
     * - (createdDate, id)를 기준으로 커서 조건 적용
     * - 정렬 조건과 동일한 기준으로 조회하여 안정적인 페이지네이션 보장
     */
    @Query("""
    SELECT c FROM Content c
    JOIN FETCH c.author
    WHERE (c.createdDate < :lastCreatedDate)
       OR (c.createdDate = :lastCreatedDate AND c.id < :lastId)
    ORDER BY c.createdDate DESC, c.id DESC
""")
    List<Content> findNextPage(
            @Param("lastCreatedDate") LocalDateTime lastCreatedDate,
            @Param("lastId") Long lastId,
            Pageable pageable
    );

    /**
     * 조회수 증가 (DB 직접 update)
     * - 엔티티 조회 없이 update 쿼리로 처리하여 성능 최적화
     */
    @Modifying
    @Query("""
    UPDATE Content c
    SET c.viewCount = c.viewCount + 1
    WHERE c.id = :id
""")
    void increaseViewCount(@Param("id") Long id);

}
