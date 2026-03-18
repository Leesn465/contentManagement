package com.malgn.content;

import com.malgn.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public void update(String title, String description, String modifier) {
        this.title = title;
        this.description = description;
        this.lastModifiedDate = LocalDateTime.now();
        this.lastModifiedBy = modifier;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

}
