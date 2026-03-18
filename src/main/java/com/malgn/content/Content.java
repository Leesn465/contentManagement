package com.malgn.content;

import com.malgn.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 콘텐츠 엔티티
 * - 사용자(User)와 다대일 관계를 가지는 도메인
 * - 생성/수정 정보(createdDate, createdBy 등)를 통해 감사(Audit) 기능을 단순 구현
 * - 최신순 조회(created_date desc, id desc)에 최적화된 인덱스를 기반으로 설계
 */
@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private Long viewCount;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false, length = 50)
    private String createdBy;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Builder
    public Content(String title,
                   String description,
                   Long viewCount,
                   LocalDateTime createdDate,
                   String createdBy,
                   LocalDateTime lastModifiedDate,
                   String lastModifiedBy,
                   User author) {
        this.title = title;
        this.description = description;
        this.viewCount = viewCount;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.lastModifiedDate = lastModifiedDate;
        this.lastModifiedBy = lastModifiedBy;
        this.author = author;
    }

    /**
     * 콘텐츠 생성 팩토리 메서드
     * - 생성 시점의 시간과 작성자 정보를 자동으로 세팅
     * - viewCount는 기본값 0으로 초기화
     */
    public static Content create(String title, String description, User author) {
        LocalDateTime now = LocalDateTime.now();

        return Content.builder()
                .title(title)
                .description(description)
                .viewCount(0L)
                .createdDate(now)
                .createdBy(author.getUsername())
                .lastModifiedDate(now)
                .lastModifiedBy(author.getUsername())
                .author(author)
                .build();
    }

    /**
     * 콘텐츠 수정
     * - 수정 시점과 수정자를 함께 기록
     */
    public void update(String title, String description, String modifier) {
        this.title = title;
        this.description = description;
        this.lastModifiedDate = LocalDateTime.now();
        this.lastModifiedBy = modifier;
    }

    /**
     * 조회수 증가
     * - 단순 증가 로직 (동시성 제어는 DB update 쿼리로 처리)
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

}
