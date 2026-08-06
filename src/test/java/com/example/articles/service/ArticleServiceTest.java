package com.example.articles.service;
import com.example.articles.dto.ArticleRequest;
import com.example.articles.dto.ArticleResponse;
import com.example.articles.dto.TagRequest;
import com.example.articles.entity.Article;
import com.example.articles.entity.Tag;
import com.example.articles.enums.ArticleStatus;
import com.example.articles.repository.ArticleRepository;
import com.example.articles.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {
    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void createArticle_記事が登録される() {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java独習", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        given(tagRepository.findByName(anyString())).willReturn(Optional.empty());
        given(articleRepository.save(any(Article.class))).willAnswer(
                invocation -> invocation.getArgument(0));

        articleService.createArticle(articleRequest);

        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        then(articleRepository).should().save(captor.capture());
        Tag firstCapturedTag = captor.getValue().getTags().get(0);
        Tag secondCapturedTag = captor.getValue().getTags().get(1);
        assertThat(captor.getValue().getTitle()).isEqualTo(articleRequest.title());
        assertThat(captor.getValue().getText()).isEqualTo(articleRequest.text());
        assertThat(captor.getValue().getStatus()).isEqualTo(articleRequest.status());
        assertThat(captor.getValue().getPublicationDate()).isEqualTo(articleRequest.publicationDate());
        assertThat(captor.getValue().getTags()).hasSize(2);

        assertThat(firstCapturedTag.getName()).isEqualTo(tagRequests.get(0).name());
        assertThat(firstCapturedTag.getArticles()).containsExactly(captor.getValue());
        assertThat(secondCapturedTag.getName()).isEqualTo(tagRequests.get(1).name());
        assertThat(secondCapturedTag.getArticles()).containsExactly(captor.getValue());
    }

    @Test
    void getArticles_pageableに合わせて記事取得() {
        List<Tag> javaTags = List.of(Tag.create("C#"), Tag.create("プログラミング"));
        List<Tag> philosophyTags = List.of(Tag.create("アリストテレス"), Tag.create("哲学"));
        Article draftArticle = Article.create("C#独習", "C#ができるには",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), javaTags);
        Article publishedArticle = Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.PUBLISHED, LocalDate.of(2026, 8, 1), philosophyTags);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = new PageImpl<>(
                List.of(draftArticle, publishedArticle), pageable, 2);
        given(articleRepository.findAll(pageable)).willReturn(articlePage);

        Page<ArticleResponse> result = articleService.getArticles(pageable);

        then(articleRepository).should().findAll(pageable);
        assertThat(result.getContent()).containsExactlyInAnyOrder(
                ArticleResponse.from(draftArticle), ArticleResponse.from(publishedArticle));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

}
