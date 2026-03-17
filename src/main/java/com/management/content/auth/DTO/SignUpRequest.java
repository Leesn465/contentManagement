package com.management.content.auth.DTO;

public record SignUpRequest(
        Long id, String username,
        String password) {
}
