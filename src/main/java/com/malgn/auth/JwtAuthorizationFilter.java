package com.malgn.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인가 필터 (Authorization Filter)
 * - 모든 요청에 대해 JWT 토큰을 검증하는 필터
 * - 인증이 필요한 요청에서 SecurityContext에 사용자 인증 정보를 설정
 * 동작 흐름:
 * 1. 요청 헤더에서 Authorization 토큰 추출
 * 2. JWT 토큰 유효성 검증
 * 3. 토큰에서 username 추출
 * 4. UserDetailsService를 통해 사용자 정보 조회
 * 5. Authentication 객체 생성 후 SecurityContext에 저장
 * 특징:
 * - OncePerRequestFilter를 상속하여 요청당 1번만 실행
 * - 유효하지 않은 토큰일 경우 인증 없이 다음 필터로 진행 (예외 발생 X)
 */
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final PrincipalUserDetailsService principalUserDetailsService;

    /**
     * 요청마다 실행되는 필터
     * - Authorization 헤더에서 토큰 추출
     * - 토큰 검증 후 사용자 정보 로드
     * - SecurityContext에 인증 객체 저장
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(JwtConstants.HEADER_STRING);

        if (header == null || !header.startsWith(JwtConstants.TOKEN_PREFIX)){
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.replace(JwtConstants.TOKEN_PREFIX, "");

        if (jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.extractUsername(token);
            UserDetails userDetails = principalUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
