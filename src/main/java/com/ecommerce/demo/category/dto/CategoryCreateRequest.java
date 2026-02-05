package com.ecommerce.demo.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 150) String slug,
        String description
) {}
