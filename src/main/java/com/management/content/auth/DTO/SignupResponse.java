package com.management.content.auth.DTO;

public record SignupResponse(
        Long id,
        String username,
        String role
) {
}
