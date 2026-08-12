package com.example.articles;

import com.example.articles.dto.ArticleRequest;
import com.example.articles.dto.ArticleResponse;
import com.example.articles.dto.TagRequest;
import com.example.articles.entity.Article;
import com.example.articles.entity.Tag;
import com.example.articles.enums.ArticleStatus;
import com.example.articles.repository.ArticleRepository;
import com.example.articles.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ArticleIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private Tag aristotleTag;
    private Tag philosophyTag;
    private Article aristotleArticle;

    @BeforeEach
    void setUp() {
        aristotleTag = Tag.create("アリストテレス");
        philosophyTag = Tag.create("哲学");
        List<Tag> philosophyTags = List.of(aristotleTag, philosophyTag);
        aristotleArticle = Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.PUBLISHED, LocalDate.of(2026, 8, 1),
                philosophyTags);

        aristotleArticle = articleRepository.save(aristotleArticle);
    }

    @Test
    void POST_articles_201_記事が登録される() throws Exception {
        List<TagRequest> tagRequests = List.of(
                new TagRequest("Java"), new TagRequest("プログラミング"));
        ArticleRequest articleRequest = new ArticleRequest("Java独習", "Java基礎",
                ArticleStatus.DRAFT, tagRequests, LocalDate.of(2026, 8, 1));
        String json = objectMapper.writeValueAsString(articleRequest);

        String responseBody = mockMvc.perform(post("/articles")
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
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        Long id = responseJson.get("id").asLong();

        entityManager.flush();
        entityManager.clear();

        Article createdArticle = articleRepository.findById(id).orElseThrow();
        Tag firstTag = createdArticle.getTags().get(0);
        Tag secondTag = createdArticle.getTags().get(1);

        assertThat(articleRepository.findAll()).hasSize(2);
        assertThat(createdArticle.getTitle()).isEqualTo(articleRequest.title());
        assertThat(createdArticle.getText()).isEqualTo(articleRequest.text());
        assertThat(createdArticle.getStatus()).isEqualTo(articleRequest.status());
        assertThat(createdArticle.getPublicationDate()).isEqualTo(articleRequest.publicationDate());
        assertThat(createdArticle.getTags()).hasSize(2);

        assertThat(firstTag.getName()).isEqualTo(tagRequests.get(0).name());
        assertThat(secondTag.getName()).isEqualTo(tagRequests.get(1).name());
    }

    @Test
    void GET_article_200_keywordに合った記事を取得() throws Exception {
        String keyword = "アリストテレス";
        mockMvc.perform(get("/articles")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value(aristotleArticle.getTitle()))
                .andExpect(jsonPath("$.content[0].text").value(aristotleArticle.getText()))
                .andExpect(jsonPath("$.content[0].status").value(String.valueOf(aristotleArticle.getStatus())))
                .andExpect(jsonPath("$.content[0].publicationDate").value(String.valueOf(aristotleArticle.getPublicationDate())))
                .andExpect(jsonPath("$.content[0].tags", hasSize(2)))
                .andExpect(jsonPath("$.content[0].tags[0].name").value(aristotleTag.getName()))
                .andExpect(jsonPath("$.content[0].tags[1].name").value(philosophyTag.getName()));
    }

}
