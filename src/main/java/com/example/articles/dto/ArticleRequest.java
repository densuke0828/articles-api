package com.example.articles.dto;

import com.example.articles.entity.Tag;
import com.example.articles.enums.ArticleStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ArticleRequest(
        @NotBlank(message = "タイトルは必須です")
        @Size(max = 50, message = "タイトルは50文字以下で入力してください")
        String title,

        @NotBlank
        @Size(max = 500)
        String text,

        @NotNull
        ArticleStatus status,

        @NotEmpty
        @Valid
        List<TagRequest> tags,

        LocalDate publicationDate
) {}
