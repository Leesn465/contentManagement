package com.management.content.user;

import com.management.content.auth.DTO.SignUpRequest;
import com.management.content.auth.DTO.SignupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "username과 password로 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public SignupResponse signup(@RequestBody SignUpRequest request){
        return userService.signUp(request);
    }
    @Operation(summary = "로그인", description = "로그인 성공 시 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public void login() {
        // 실제 로직은 JwtAuthenticationFilter에서 처리됨
    }


}
