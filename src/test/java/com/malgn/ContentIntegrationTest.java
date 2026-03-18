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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🔥 JWT 발급 헬퍼
    private String getToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

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