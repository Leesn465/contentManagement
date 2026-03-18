package com.malgn.auth.DTO;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
