package com.malgn.exception;

/**
 * 권한 없음 예외 (403)
 * - 작성자 또는 관리자 권한 검증 실패 시 사용
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}