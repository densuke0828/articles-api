package com.example.articles.controller;

import com.example.articles.dto.ArticleRequest;
import com.example.articles.dto.ArticleResponse;
import com.example.articles.dto.TagRequest;
import com.example.articles.entity.Article;
import com.example.articles.entity.Tag;
import com.example.articles.enums.ArticleStatus;
import com.example.articles.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(ArticleController.class)
public class ArticleControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ArticleService articleService;

    @Autowired
    ObjectMapper objectMapper;

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
    void createArticle_201() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java独習", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        List<Tag> createdTags = List.of(Tag.create("Java"), Tag.create("プログラミング"));
        Article createdArticle = Article.create("Java独習", "Java基礎",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), createdTags);

        String json = objectMapper.writeValueAsString(articleRequest);
        given(articleService.createArticle(any(ArticleRequest.class)))
                .willReturn(createdArticle);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(articleRequest.title()))
                .andExpect(jsonPath("$.text").value(articleRequest.text()))
                .andExpect(jsonPath("$.status").value(String.valueOf(articleRequest.status())))
                .andExpect(jsonPath("$.publicationDate").value(String.valueOf(articleRequest.publicationDate())))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[0].name").value(tagRequests.get(0).name()))
                .andExpect(jsonPath("$.tags[1].name").value(tagRequests.get(1).name()))
                .andExpect(header().exists("Location"));

        then(articleService).should().createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Article_titleが51文字以上() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("あ".repeat(51), "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("title")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Article_titleが空文字() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("title")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Article_textが501文字以上() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java", "あ".repeat(501),
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("text")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Article_textが空文字() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java", "",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("text")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Article_statusがnull() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java", "Java基礎",
                null, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("status")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Article_tagsが空リスト() throws Exception {
        List<TagRequest> tagRequests = List.of();
        ArticleRequest articleRequest = new ArticleRequest("Java", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("tags")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Article_isTagsUnique_タグ名が重複している() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("Java"));
        ArticleRequest articleRequest = new ArticleRequest("Java", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("tagsUnique")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Tag_nameが21文字以上() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("あ".repeat(21)), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("tags[0].name")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Tag_1番目のnameが空文字() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest(""), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("tags[0].name")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void createArticle_400_Tag_2番目のnameが空文字() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest(""));
        ArticleRequest articleRequest = new ArticleRequest("Java", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("tags[1].name")));
        then(articleService).should(never()).createArticle(articleRequest);
    }

    @Test
    void getArticle_200_keywordに合致した記事を取得() throws Exception {
        String keyword = "アリストテレス";
        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleResponse> articlePages = new PageImpl<>(
                List.of(ArticleResponse.from(aristotleArticle)), pageable, 1);
        given(articleService.searchByKeyword(keyword, pageable)).willReturn(articlePages);

        mockMvc.perform(get("/articles").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value(aristotleArticle.getTitle()))
                .andExpect(jsonPath("$.content[0].text").value(aristotleArticle.getText()))
                .andExpect(jsonPath("$.content[0].status").value(String.valueOf(aristotleArticle.getStatus())))
                .andExpect(jsonPath("$.content[0].publicationDate").value(String.valueOf(aristotleArticle.getPublicationDate())))
                .andExpect(jsonPath("$.content[0].tags", hasSize(2)))
                .andExpect(jsonPath("$.content[0].tags[0].name").value(philosophyTags.get(0).getName()));
    }

    @Test
    void getArticle_200_pageableに合わせた記事を取得() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleResponse> articlePages = new PageImpl<>(
                List.of(ArticleResponse.from(aristotleArticle)), pageable, 1);
        given(articleService.getArticles(pageable)).willReturn(articlePages);

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value(aristotleArticle.getTitle()))
                .andExpect(jsonPath("$.content[0].text").value(aristotleArticle.getText()))
                .andExpect(jsonPath("$.content[0].status").value(String.valueOf(aristotleArticle.getStatus())))
                .andExpect(jsonPath("$.content[0].publicationDate").value(String.valueOf(aristotleArticle.getPublicationDate())))
                .andExpect(jsonPath("$.content[0].tags", hasSize(2)))
                .andExpect(jsonPath("$.content[0].tags[0].name").value(philosophyTags.get(0).getName()));
    }

    @Test
    void getArticleById_200_指定されたidの記事を取得() throws Exception {
        given(articleService.getArticleById(1L)).willReturn(aristotleArticle);

        mockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(aristotleArticle.getTitle()))
                .andExpect(jsonPath("$.text").value(aristotleArticle.getText()))
                .andExpect(jsonPath("$.status").value(String.valueOf(aristotleArticle.getStatus())))
                .andExpect(jsonPath("$.publicationDate").value(String.valueOf(aristotleArticle.getPublicationDate())))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[0].name").value(philosophyTags.get(0).getName()))
                .andExpect(jsonPath("$.tags[1].name").value(philosophyTags.get(1).getName()));
    }
}
