package com.malgn.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.malgn.user.Role;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증 클래스
 * - 토큰 생성(createToken)
 * - 토큰 검증(validateToken)
 * - 사용자 정보 추출(extractUsername)
 */
@Component
public class JwtTokenProvider {

    /**
     * JWT 토큰 생성
     * - 사용자 ID, username, role을 클레임으로 포함
     * - 만료 시간 및 issuer 설정
     */
    public String createToken(Long userId,String username, Role role){
        long now = System.currentTimeMillis();

        return JWT.create()
                .withSubject(username)
                .withIssuer(JwtConstants.ISSUER)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + JwtConstants.ACCESS_TOKEN_EXPIRATION_MILLIS))
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("userId",userId)
                .withClaim("username",username)
                .withClaim("role",role.name())
                .sign(Algorithm.HMAC512(JwtConstants.SECRET_KEY));


    }
    public String extractUsername(String token) {
        return JWT.require(Algorithm.HMAC512(JwtConstants.SECRET_KEY))
                .withIssuer(JwtConstants.ISSUER)
                .build()
                .verify(token)
                .getClaim("username")
                .asString();
    }

    /**
     * JWT 토큰 유효성 검증
     * - 서명 및 만료 시간 검증
     */
    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC512(JwtConstants.SECRET_KEY))
                    .withIssuer(JwtConstants.ISSUER)
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
