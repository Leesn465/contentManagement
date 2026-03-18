package com.malgn.content.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentUpdateRequest(

        @Schema(description = "수정할 제목", example = "일본의 인구 수는?")
        @NotBlank
        @Size(max = 100)
        String title,

        @Schema(description = "수정할 내용", example = "2026년 2월 기준 일본의 총인구는 약 1억 2천만명이다." +
                "2026년 기준 세계 12위이다. ")
        String description
){
}
