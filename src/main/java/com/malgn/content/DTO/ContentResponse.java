package com.malgn.content.DTO;


import com.malgn.content.Content;

import java.time.LocalDateTime;

public record ContentResponse(
        Long id,
        String title,
        String description,
        Long viewCount,
        LocalDateTime createdDate,
        String createdBy,
        LocalDateTime lastModifiedDate,
        String lastModifiedBy
) {
    public static ContentResponse from(Content content) {
        return new ContentResponse(
                content.getId(),
                content.getTitle(),
                content.getDescription(),
                content.getViewCount(),
                content.getCreatedDate(),
                content.getCreatedBy(),
                content.getLastModifiedDate(),
                content.getLastModifiedBy()
        );
    }
}
