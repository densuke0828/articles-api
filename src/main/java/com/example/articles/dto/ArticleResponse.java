package com.example.articles.dto;

import com.example.articles.entity.Article;
import com.example.articles.enums.ArticleStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleResponse(
        Long id,
        String title,
        String text,
        ArticleStatus status,
        List<TagResponse> tags,
        LocalDate publicationDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ArticleResponse from(Article article) {
        List<TagResponse> tagResponses = article.getTags().stream()
                .map(TagResponse::from)
                .toList();

        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getText(),
                article.getStatus(),
                tagResponses,
                article.getPublicationDate(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
