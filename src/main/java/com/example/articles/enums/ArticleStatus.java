package com.example.articles.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ArticleStatus {
    DRAFT("下書き"),
    PUBLISHED("公開済み");

    private final String displayName;
}
