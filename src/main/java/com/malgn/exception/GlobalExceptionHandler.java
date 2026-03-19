package com.malgn.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * - 애플리케이션 전반에서 발생하는 예외를 공통 포맷으로 응답
 * - HTTP 상태 코드와 메시지를 일관되게 관리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                e.getMessage(),
                request.getRequestURI()
        );
        return org.springframework.http.ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        return org.springframework.http.ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 유효성 검증 실패 처리
     * - @Valid 검증 실패 시 첫 번째 에러 메시지 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("잘못된 요청입니다.");

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                message,
                request.getRequestURI()
        );
        return org.springframework.http.ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                "요청 본문이 올바르지 않습니다.",
                request.getRequestURI()
        );
        return org.springframework.http.ResponseEntity.badRequest().body(response);
    }

    /**
     * 인증 실패 처리
     * - 로그인 실패 또는 사용자 조회 실패 시 동일 메시지 반환
     * - 보안상 구체적인 원인을 노출하지 않음
     */
    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            InternalAuthenticationServiceException.class
    })
    public org.springframework.http.ResponseEntity<ErrorResponse> handleAuthentication(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "아이디 또는 비밀번호가 올바르지 않습니다.",
                request.getRequestURI()
        );
        return org.springframework.http.ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 기타 예외 처리
     * - 예상하지 못한 서버 오류 처리
     */
    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                request.getRequestURI()
        );
        return org.springframework.http.ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    @ExceptionHandler(ForbiddenException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException e,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                e.getMessage(),
                request.getRequestURI()
        );
        return org.springframework.http.ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

}