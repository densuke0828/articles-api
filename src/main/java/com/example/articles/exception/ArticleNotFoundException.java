package com.example.articles.exception;

public class ArticleNotFoundException extends RuntimeException {
    public ArticleNotFoundException(Long id) {
        super("記事ID: " + id + "が見つかりません");
    }

    public String getUserMessage() {
        return "指定された記事はありません";
    }
}
