package com.management.content.auth.DTO;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
