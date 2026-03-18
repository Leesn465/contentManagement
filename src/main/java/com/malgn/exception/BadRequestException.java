package com.malgn.exception;

/**
 * 잘못된 요청 예외 (400)
 * - 비즈니스 로직 검증 실패 시 사용
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}