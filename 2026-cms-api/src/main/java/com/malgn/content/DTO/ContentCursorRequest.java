package com.malgn.content.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ContentCursorRequest(
        @Schema(description = "마지막 조회된 생성일", example = "2026-03-19T10:30:00")
        LocalDateTime lastCreatedDate,

        @Schema(description = "마지막 조회된 ID", example = "100")
        Long lastId,

        @Schema(description = "조회 개수", example = "10")
        int size
) {
}
