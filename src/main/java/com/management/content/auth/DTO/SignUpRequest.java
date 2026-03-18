package com.management.content.auth.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "username은 필수입니다.")
        @Size(min = 4, max = 50, message = "username은 4자 이상 50자 이하여야 합니다.")
        String username,

        @NotBlank(message = "password는 필수입니다.")
        @Size(min = 4, max = 100, message = "password는 4자 이상 100자 이하여야 합니다.")
        String password
) {
}