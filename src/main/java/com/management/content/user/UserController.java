package com.management.content.user;

import com.management.content.auth.DTO.SignUpRequest;
import com.management.content.auth.DTO.SignupResponse;
import com.management.content.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Operation(
            summary = "내 정보 조회",
            description = "Authorization 헤더의 JWT 토큰을 기반으로 현재 로그인한 사용자의 정보를 조회합니다."
    )
    @GetMapping("/me")
    public String me(@AuthenticationPrincipal PrincipalDetails userDetails) {
        return userDetails.getUsername();
    }


}
