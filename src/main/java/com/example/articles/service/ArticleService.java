package com.example.articles.service;

import com.example.articles.dto.ArticleRequest;
import com.example.articles.entity.Article;
import com.example.articles.entity.Tag;
import com.example.articles.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = false)
    public Article createArticle(ArticleRequest request) {
        List<Tag> tags = request.tags().stream()
                .map(req -> Tag.create(req.name()))
                .toList();
        return articleRepository.save(Article.create(request.title(), request.text(),
                request.status(), request.publicationDate(), tags));
    }
}
