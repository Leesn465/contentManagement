package com.management.content.auth.DTO;

public record LoginRequest (
        String username,
        String password
){
}
