package com.management.content.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content,Long> {
    // Page<Content> findAllByOrderByCreatedDateDesc(Pageable pageable);
    @Query("""
    SELECT c FROM Content c
    ORDER BY c.createdDate DESC, c.id DESC
""")
    List<Content> findFirstPage(Pageable pageable);

    @Query("""
    SELECT c FROM Content c
    WHERE (c.createdDate < :lastCreatedDate)
       OR (c.createdDate = :lastCreatedDate AND c.id < :lastId)
    ORDER BY c.createdDate DESC, c.id DESC
""")
    List<Content> findNextPage(
            @Param("lastCreatedDate") LocalDateTime lastCreatedDate,
            @Param("lastId") Long lastId,
            Pageable pageable
    );
}
