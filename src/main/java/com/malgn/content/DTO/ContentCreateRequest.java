package com.malgn.content.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentCreateRequest(
        @Schema(description = "콘텐츠 제목", example = "대한민국의 인구 수는?")
        @NotBlank
        @Size(max = 100)
        String title,

        @Schema(description = "콘텐츠 내용", example = "2026년 2월 기준 대한민국의 총인구는 약 5,110만 명이다." +
                "2026년 기준 세계 30위이다. ")
        String description
){
}
