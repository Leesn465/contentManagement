package com.malgn.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.malgn.user.Role;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

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
