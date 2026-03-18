package com.malgn.auth.DTO;

public record SignupResponse(
        Long id,
        String username,
        String role
) {
}
