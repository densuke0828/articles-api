package com.example.articles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank
        @Size(max = 20)
        String name
) {}
