package com.example.articles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "タグ名は必須です")
        @Size(max = 20, message = "タグ名は20文字以下で入力してください")
        String name
) {}
