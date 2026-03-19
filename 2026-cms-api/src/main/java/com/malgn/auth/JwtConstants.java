package com.malgn.auth;

/**
 * JWT 관련 상수 정의
 * - SECRET_KEY: 토큰 서명 키
 * - TOKEN_PREFIX: Authorization 헤더 접두사 (Bearer)
 * - HEADER_STRING: 토큰 헤더 이름
 */
public class JwtConstants {
    public static final long ACCESS_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60; // 1시간
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ISSUER = "content-cms";
}
