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

        @NotBlank
        @Size(max = 500)
        String text,

        @NotNull
        ArticleStatus status,

        @NotEmpty
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
