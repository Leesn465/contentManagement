package com.malgn.auth;

public class JwtConstants {
    public static final String SECRET_KEY = "my-super-secret-key-for-cms-project-2026";
    public static final long ACCESS_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60; // 1시간
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ISSUER = "content-cms";
}
