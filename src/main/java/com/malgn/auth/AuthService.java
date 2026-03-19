package com.malgn.auth;

import com.malgn.auth.DTO.LoginRequest;
import com.malgn.auth.DTO.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 인증 서비스
 * - 사용자 로그인 요청을 처리하고 JWT 토큰을 발급하는 역할
 * - Spring Security의 AuthenticationManager를 통해 인증을 수행
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 처리
     * - 전달받은 username, password로 인증 수행
     * - 인증 성공 시 사용자 정보를 기반으로 JWT 토큰 생성 후 반환
     *
     * @param request 로그인 요청 DTO (username, password)
     * @return LoginResponse (JWT access token 포함)
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(
                principal.getId(),
                principal.getUsername(),
                principal.getRole()
        );
        return LoginResponse.of(token);
    }
}
