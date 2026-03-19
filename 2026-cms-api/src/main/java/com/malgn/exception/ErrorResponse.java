package com.malgn.exception;


import java.time.LocalDateTime;

/**
 * 공통 에러 응답 객체
 * - 모든 예외를 동일한 JSON 구조로 반환
 * - 클라이언트에서 일관된 에러 처리 가능
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    /**
     * ErrorResponse 생성 팩토리 메서드
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path
        );
    }
}