package com.malgn.auth;


import com.malgn.auth.DTO.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;


/**
 * JWT 인증 필터 (로그인 처리)
 * - /api/auth/login 요청을 가로채어 인증 수행
 * - 인증 성공 시 JWT 토큰 생성 및 응답 반환
 */
@RequiredArgsConstructor
@NullMarked
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 로그인 요청 처리
     * - JSON body에서 username/password 추출
     * - AuthenticationManager를 통해 인증 수행
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    );

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    /**
     * 인증 성공 시 JWT 토큰 생성
     * - 사용자 정보를 기반으로 accessToken 생성
     * - Authorization 헤더 및 응답 body에 토큰 포함
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        PrincipalDetails userDetails = (PrincipalDetails) authResult.getPrincipal();

        String token = jwtTokenProvider.createToken(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole()
        );

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader(JwtConstants.HEADER_STRING, JwtConstants.TOKEN_PREFIX + token);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "accessToken", token,
                "tokenType", "Bearer"
        )));
    }
}
