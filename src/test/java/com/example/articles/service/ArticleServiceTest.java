package com.example.articles.service;
import com.example.articles.dto.ArticleRequest;
import com.example.articles.dto.ArticleResponse;
import com.example.articles.dto.TagRequest;
import com.example.articles.entity.Article;
import com.example.articles.entity.Tag;
import com.example.articles.enums.ArticleStatus;
import com.example.articles.exception.ArticleNotFoundException;
import com.example.articles.repository.ArticleRepository;
import com.example.articles.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;

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

    private List<Tag> philosophyTags;
    private Article aristotleArticle;

    @BeforeEach
    void setUp() {
        philosophyTags = List.of(Tag.create("アリストテレス"), Tag.create("哲学"));
        aristotleArticle = Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.PUBLISHED, LocalDate.of(2026, 8, 1),
                philosophyTags);
    }

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
        List<Tag> goTags = List.of(Tag.create("GO"), Tag.create("プログラミング"));
        Article goArticle = Article.create("GO独習", "GOができるには",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), goTags);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePages = new PageImpl<>(
                List.of(goArticle, aristotleArticle), pageable, 2);
        given(articleRepository.findAll(pageable)).willReturn(articlePages);

        Page<ArticleResponse> result = articleService.getArticles(pageable);

        then(articleRepository).should().findAll(pageable);
        assertThat(result.getContent()).containsExactlyInAnyOrder(
                ArticleResponse.from(goArticle), ArticleResponse.from(aristotleArticle));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void searchByKeyword_keywordに合致した記事を取得() {
        String keyword = "アリストテレス";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePages = new PageImpl<>(List.of(aristotleArticle), pageable, 1);
        given(articleRepository.findByKeyword(keyword, pageable)).willReturn(articlePages);

        Page<ArticleResponse> result = articleService.searchByKeyword(keyword, pageable);

        then(articleRepository).should().findByKeyword(keyword, pageable);
        assertThat(result.getContent()).containsExactlyInAnyOrder(
                ArticleResponse.from(aristotleArticle));
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getArticleById_正常系_指定したIdの記事を取得() {
        given(articleRepository.findById(1L)).willReturn(Optional.of(aristotleArticle));

        Article result = articleService.getArticleById(1L);

        then(articleRepository).should().findById(1L);
        assertThat(result).isEqualTo(aristotleArticle);
    }

    @Test
    void getArticleById_異常系_指定したIDが登録されていない() {
        given(articleRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.getArticleById(999L))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessageContaining("999");
        then(articleRepository).should().findById(999L);
    }

    @Test
    void updateArticle_正常系_記事が更新される() {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("プラトン"), new TagRequest("ギリシャ哲学"));
        ArticleRequest articleRequest = new ArticleRequest("プラトン入門", "プラトンとは",
                ArticleStatus.PUBLISHED, tagRequests, LocalDate.of(2026, 8, 8));
        given(articleRepository.findById(1L)).willReturn(Optional.of(aristotleArticle));
        given(tagRepository.findByName(anyString())).willReturn(Optional.empty());

        Article result = articleService.updateArticle(1L, articleRequest);

        then(articleRepository).should().findById(1L);
        Tag firstTag = result.getTags().get(0);
        Tag secondTag = result.getTags().get(1);

        assertThat(result.getTitle()).isEqualTo(articleRequest.title());
        assertThat(result.getText()).isEqualTo(articleRequest.text());
        assertThat(result.getStatus()).isEqualTo(articleRequest.status());
        assertThat(result.getPublicationDate()).isEqualTo(articleRequest.publicationDate());
        assertThat(result.getTags()).hasSize(2);

        assertThat(firstTag.getName()).isEqualTo(tagRequests.get(0).name());
        assertThat(firstTag.getArticles()).containsExactly(result);
        assertThat(secondTag.getName()).isEqualTo(tagRequests.get(1).name());
        assertThat(secondTag.getArticles()).containsExactly(result);

        assertThat(philosophyTags.get(0).getArticles()).doesNotContain(result);
        assertThat(philosophyTags.get(1).getArticles()).doesNotContain(result);
    }

    @Test
    void updateArticle_異常系_指定したIDが登録されていない() {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("プラトン"), new TagRequest("ギリシャ哲学"));
        ArticleRequest articleRequest = new ArticleRequest("プラトン入門", "プラトンとは",
                ArticleStatus.PUBLISHED, tagRequests, LocalDate.of(2026, 8, 8));
        given(articleRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.updateArticle(999L, articleRequest))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessageContaining("999");
        then(articleRepository).should().findById(999L);
    }

    @Test
    void deleteArticle_正常系_記事が削除される() {
        given(articleRepository.findById(1L)).willReturn(Optional.of(aristotleArticle));

        articleService.deleteArticle(1L);

        then(articleRepository).should().findById(1L);
        then(articleRepository).should().delete(aristotleArticle);
    }

    @Test
    void deleteArticle_異常系_指定したIDが登録されていない() {
        given(articleRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.deleteArticle(999L))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessageContaining("999");
        then(articleRepository).should().findById(999L);
        then(articleRepository).should(never()).delete(any());
    }
}
