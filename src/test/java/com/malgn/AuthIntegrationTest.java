package com.malgn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.malgn.user.Role;
import com.malgn.user.User;
import com.malgn.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 인증(Auth) 통합 테스트
 * - 회원가입, 로그인, JWT 인증 흐름을 검증
 * - 실제 필터 체인까지 포함한 end-to-end 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 회원가입 성공 테스트
     * - 정상 요청 시 사용자 생성 및 username 반환 확인
     */
    @Test
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        // given
        String requestBody = """
                {
                  "username": "user1",
                  "password": "user1234"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.role").value("USER"));

        User savedUser = userRepository.findByUsername("user1").orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("user1");
        assertThat(passwordEncoder.matches("user1234", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }
    /**
     * 회원가입 실패 - 요청값 검증 오류
     * - username, password 유효성 검증 실패 시 VALIDATION_FAILED 반환
     */
    @Test
    @DisplayName("회원가입 실패 - 요청값 검증 오류")
    void signup_fail_validation() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username": "",
                      "password": "1234"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    /**
     * 회원가입 실패 - 중복 username
     * - 동일 username 존재 시 BAD_REQUEST 반환
     */
    @Test
    @DisplayName("중복 username 회원가입 실패")
    void signup_fail_duplicate_username() throws Exception {
        // given
        userRepository.save(User.builder()
                .username("user1")
                .password(passwordEncoder.encode("user1234"))
                .role(Role.USER)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build());

        String requestBody = """
            {
              "username": "user1",
              "password": "user9999"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 username입니다."));
    }

    /**
     * 로그인 성공 테스트
     * - 인증 성공 시 JWT 토큰 발급 확인
     */
    @Test
    @DisplayName("로그인 성공 시 JWT 토큰 발급")
    void login_success() throws Exception {
        // given
        userRepository.save(User.builder()
                .username("user1")
                .password(passwordEncoder.encode("user1234"))
                .role(Role.USER)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build());

        String requestBody = """
                {
                  "username": "user1",
                  "password": "user1234"
                }
                """;

        // when
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String accessToken = jsonNode.get("accessToken").asText();

        assertThat(accessToken).isNotBlank();
    }

    /**
     * 로그인 실패 - 비밀번호 불일치
     * - 인증 실패 시 UNAUTHORIZED 반환
     */
    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrong_password() throws Exception {
        // given
        userRepository.save(User.builder()
                .username("user1")
                .password(passwordEncoder.encode("user1234"))
                .role(Role.USER)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build());

        String requestBody = """
                {
                  "username": "user1",
                  "password": "wrong-password"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 인증 없이 보호 API 접근
     * - JWT 토큰이 없을 경우 접근 차단 (403)
     */
    @Test
    @DisplayName("JWT 없이 보호 API 접근 실패")
    void access_protected_api_without_token() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    /**
     * JWT 인증 성공 테스트
     * - 발급받은 토큰으로 보호 API 접근 가능 확인
     */
    @Test
    @DisplayName("JWT로 보호 API 접근 성공")
    void access_protected_api_with_token() throws Exception {
        // given
        userRepository.save(User.builder()
                .username("user1")
                .password(passwordEncoder.encode("user1234"))
                .role(Role.USER)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build());

        String loginRequest = """
                {
                  "username": "user1",
                  "password": "user1234"
                }
                """;

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("user1"));
    }
}