package com.example.articles.service;

import com.example.articles.dto.ArticleRequest;
import com.example.articles.dto.ArticleResponse;
import com.example.articles.entity.Article;
import com.example.articles.entity.Tag;
import com.example.articles.exception.ArticleNotFoundException;
import com.example.articles.repository.ArticleRepository;
import com.example.articles.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = false)
    public Article createArticle(ArticleRequest request) {
        List<Tag> tags = request.tags().stream()
                .map(req -> tagRepository.findByName(req.name())
                        .orElseGet(() -> Tag.create(req.name())))
                .toList();
        return articleRepository.save(Article.create(request.title(), request.text(),
                request.status(), request.publicationDate(), tags));
    }

    public Page<ArticleResponse> getArticles(Pageable pageable) {
        return articleRepository.findAll(pageable)
                .map(ArticleResponse::from);
    }

    public Page<ArticleResponse> searchByKeyword(String keyword, Pageable pageable) {
        return articleRepository.findByKeyword(keyword, pageable)
                .map(ArticleResponse::from);
    }

    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }
}