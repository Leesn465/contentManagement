package com.malgn;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.malgn.auth.DTO.LoginRequest;
import com.malgn.auth.DTO.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 콘텐츠(Content) 통합 테스트
 * - 콘텐츠 생성, 수정, 권한 검증, 커서 페이징을 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //  JWT 발급 헬퍼
    /**
     * 테스트용 JWT 토큰 발급 메서드
     * - 로그인 API를 호출하여 실제 토큰을 생성
     */
    private String getToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    /**
     * 콘텐츠 등록 성공 테스트
     * - 인증된 사용자만 콘텐츠 생성 가능
     */
    @Test
    @DisplayName("콘텐츠 등록 성공")
    void create_success() throws Exception {
        // given
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user1", "1234"))));

        String token = getToken("user1", "1234");

        String body = """
                {
                    "title": "제목",
                    "description": "내용"
                }
                """;

        // when + then
        mockMvc.perform(post("/api/contents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("제목"));
    }

    /**
     * 콘텐츠 등록 실패 - 인증 없음
     * - JWT 없이 요청 시 접근 차단 (403)
     */
    @Test
    @DisplayName("JWT 없이 콘텐츠 등록 실패")
    void create_fail_without_token() throws Exception {
        String body = """
            {
                "title": "제목",
                "description": "내용"
            }
            """;

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    /**
     * 콘텐츠 수정 성공
     * - 작성자는 자신의 콘텐츠를 수정할 수 있다
     */
    @Test
    @DisplayName("작성자는 자신의 콘텐츠를 수정할 수 있다")
    void update_success_owner() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user1", "1234"))));

        String token = getToken("user1", "1234");

        var createResult = mockMvc.perform(post("/api/contents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "제목",
                                "description": "내용"
                            }
                            """))
                .andReturn();

        Long contentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(put("/api/contents/" + contentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "수정된 제목",
                                "description": "수정된 내용"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.description").value("수정된 내용"));
    }

    /**
     * 콘텐츠 수정 실패 - 권한 없음
     * - 작성자가 아닌 사용자는 수정 불가 (403)
     */
    @Test
    @DisplayName("작성자가 아닌 사용자는 수정할 수 없다")
    void update_fail_notOwner() throws Exception {
        // user1 생성
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user1", "1234"))));

        // user2 생성
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user2", "1234"))));

        String token1 = getToken("user1", "1234");
        String token2 = getToken("user2", "1234");

        // 콘텐츠 생성 (user1)
        String createBody = """
                {
                    "title": "제목",
                    "description": "내용"
                }
                """;

        var result = mockMvc.perform(post("/api/contents")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andReturn();

        Long contentId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // user2가 수정 시도 → 실패
        String updateBody = """
                {
                    "title": "수정",
                    "description": "수정"
                }
                """;

        mockMvc.perform(put("/api/contents/" + contentId)
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden());
    }

    /**
     * 콘텐츠 잠금 성공
     * - ADMIN 사용자는 특정 콘텐츠를 잠글 수 있다
     * - 잠금 후 상세 조회 시 locked=true 를 반환해야 한다
     */
    @Test
    @DisplayName("관리자는 콘텐츠를 잠글 수 있다")
    void lock_success_by_admin() throws Exception {
        // given
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user1", "1234"))));

        String userToken = getToken("user1", "1234");
        String adminToken = getToken("admin", "admin1234");

        var createResult = mockMvc.perform(post("/api/contents")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "잠금 테스트 제목",
                            "description": "잠금 테스트 내용"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        Long contentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // when + then
        mockMvc.perform(patch("/api/contents/" + contentId + "/lock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/contents/" + contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true));
    }

    /**
     * 콘텐츠 잠금 권한 검증 테스트
     * - 관리자(ADMIN)가 아닌 일반 사용자는 콘텐츠를 잠글 수 없다
     * - 작성자가 아닌 다른 사용자(user2)가 잠금 요청 시도
     * - 서비스의 validateAdmin() 로직에 의해 Forbidden(403) 예외가 발생해야 한다
     */
    @Test
    @DisplayName("관리자가 아닌 사용자는 콘텐츠를 잠글 수 없다")
    void lock_fail_by_non_admin() throws Exception {
        // given
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user1", "1234"))));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user2", "1234"))));

        String ownerToken = getToken("user1", "1234");
        String otherUserToken = getToken("user2", "1234");

        var createResult = mockMvc.perform(post("/api/contents")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "제목",
                            "description": "내용"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        Long contentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // when + then
        mockMvc.perform(patch("/api/contents/" + contentId + "/lock")
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    /**
     * 잠금 상태 수정 제한
     * - 작성자는 잠긴 콘텐츠를 수정할 수 없다
     * - 관리자는 잠금 여부와 관계없이 수정 가능하다
     */
    @Test
    @DisplayName("잠긴 콘텐츠는 작성자가 수정할 수 없다")
    void update_fail_when_locked() throws Exception {
        // given
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user1", "1234"))));

        String userToken = getToken("user1", "1234");
        String adminToken = getToken("admin", "admin1234");

        var createResult = mockMvc.perform(post("/api/contents")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "원본 제목",
                            "description": "원본 내용"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        Long contentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(patch("/api/contents/" + contentId + "/lock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // when + then
        mockMvc.perform(put("/api/contents/" + contentId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "수정 시도",
                            "description": "수정 시도"
                        }
                        """))
                .andExpect(status().isForbidden());
    }


    /**
     * 커서 기반 페이징 조회 테스트
     * - 최신순 정렬 기준으로 다음 페이지 조회 가능
     * - size + 1 조회를 통해 hasNext 판단 검증
     */
    @Test
    @DisplayName("커서 기반 목록 조회 성공")
    void cursorPagination_success() throws Exception {
        // given
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest("user1", "1234"))));

        String token = getToken("user1", "1234");

        // 콘텐츠 여러 개 생성
        for (int i = 0; i < 15; i++) {
            mockMvc.perform(post("/api/contents")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "title": "title%s",
                            "description": "desc%s"
                        }
                    """.formatted(i, i)));
        }

        // when
        var first = mockMvc.perform(get("/api/contents?size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.hasNext").value(true))
                .andReturn();

        var json = objectMapper.readTree(first.getResponse().getContentAsString());

        String lastCreatedDate = json.get("nextLastCreatedDate").asText();
        long lastId = json.get("nextLastId").asLong();

        // 다음 페이지
        mockMvc.perform(get("/api/contents")
                        .param("lastCreatedDate", lastCreatedDate)
                        .param("lastId", String.valueOf(lastId))
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }
}