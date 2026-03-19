package com.malgn.auth;


import com.malgn.auth.DTO.LoginRequest;
import com.malgn.auth.DTO.LoginResponse;
import com.malgn.auth.DTO.SignUpRequest;
import com.malgn.auth.DTO.SignupResponse;
import com.malgn.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API
 * - 회원가입 및 로그인 요청 처리
 * - 로그인은 AuthenticationManager를 통해 인증 처리 후 JWT 발급
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "회원가입", description = "username과 password로 회원가입을 진행합니다.")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@RequestBody @Valid SignUpRequest request){
        return userService.signUp(request);
    }

    @Operation(summary = "로그인", description = "로그인 성공 시 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

}
