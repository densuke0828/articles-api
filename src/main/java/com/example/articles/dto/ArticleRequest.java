package com.example.articles.dto;

import com.example.articles.enums.ArticleStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ArticleRequest(
        @NotBlank(message = "タイトルは必須です")
        @Size(max = 50, message = "タイトルは50文字以下で入力してください")
        String title,

        @NotBlank(message = "テキストは必須です")
        @Size(max = 500, message = "テキストは500文字以下で入力してください")
        String text,

        @NotNull(message = "ステータスは必須です")
        ArticleStatus status,

        @NotEmpty(message = "タグは必須です")
        @Valid
        List<TagRequest> tags,

        LocalDate publicationDate
) {
    @AssertTrue(message = "タグ名が重複しています")
    public boolean isTagsUnique() {
        long distinctCount = tags.stream().map(TagRequest::name).distinct().count();
        return distinctCount == tags.size();
    }
}
