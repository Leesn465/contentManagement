package com.malgn.configure.security;


//import com.malgn.auth.JwtAuthenticationFilter;
import com.malgn.auth.JwtAuthorizationFilter;
import com.malgn.auth.JwtTokenProvider;
import com.malgn.auth.PrincipalUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Spring Security 보안 설정
 * - JWT 기반 Stateless 인증 방식을 적용
 * - 공개 API와 인증이 필요한 API의 접근 권한을 분리
 * - 로그인은 AuthController/AuthService에서 처리하고,
 *   이후 요청의 JWT 검증은 JwtAuthorizationFilter에서 수행
 * 주요 설정:
 * - csrf, formLogin, httpBasic 비활성화
 * - 세션을 사용하지 않는 Stateless 방식 적용
 * - JWT 인가 필터를 UsernamePasswordAuthenticationFilter 이전에 등록
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;
    private final PrincipalUserDetailsService principalUserDetailsService;

    /**
     * Spring Security 인증 매니저 등록
     * - 로그인 필터에서 사용자 인증 처리 시 사용
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 구성
     * 공개 API:
     * - 회원가입, 로그인
     * - Swagger 문서 조회
     * - 콘텐츠 조회(GET)
     * 보호 API:
     * - 그 외 모든 요청은 인증 필요
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

//        AuthenticationManager authenticationManager = authenticationManager();

//        JwtAuthenticationFilter jwtAuthenticationFilter =
//                new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider);

        http
                .csrf(AbstractHttpConfigurer::disable) // REST API 기반 구조이므로 CSRF 비활성화
                .cors(AbstractHttpConfigurer::disable) // 별도 CORS 설정이 없으므로 기본 CORS 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 방식 비활성화 (JWT 사용)
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 서버 세션을 사용하지 않는 Stateless 인증 방식 적용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/contents/**").permitAll()
                        .anyRequest().authenticated()
                )
                // JWT 토큰 검증 필터 등록
                // UsernamePasswordAuthenticationFilter 이전에 실행되어
                // 요청 헤더의 토큰을 먼저 검증하고 SecurityContext에 인증 정보 저장
                .addFilterBefore(
                        new JwtAuthorizationFilter(jwtTokenProvider, principalUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}