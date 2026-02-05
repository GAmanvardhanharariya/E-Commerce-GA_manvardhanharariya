package com.ecommerce.demo.auth.dto;

public record TokenResponse(
        String accessToken,
        String tokenType
) {}
