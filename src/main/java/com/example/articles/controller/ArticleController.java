package com.example.articles.controller;

import com.example.articles.dto.ArticleRequest;
import com.example.articles.dto.ArticleResponse;
import com.example.articles.entity.Article;
import com.example.articles.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(
            @Valid @RequestBody ArticleRequest request) {
        Article createdArticle = articleService.createArticle(request);
        return ResponseEntity
                .created(URI.create("/articles/" + createdArticle.getId()))
                .body(ArticleResponse.from(createdArticle));
    }

    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> getArticles(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(articleService.getArticles(pageable));
    }
}
