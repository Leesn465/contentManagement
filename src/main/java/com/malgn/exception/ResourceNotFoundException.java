package com.malgn.exception;

/**
 * 리소스 조회 실패 예외 (404)
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}