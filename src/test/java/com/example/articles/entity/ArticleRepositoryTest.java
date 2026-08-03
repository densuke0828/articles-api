package com.example.articles.entity;

import com.example.articles.enums.ArticleStatus;
import com.example.articles.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class ArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;
    private List<Tag> javaTags;
    private List<Tag> philosophyTags;

    @BeforeEach
    void setUp() {
        javaTags = List.of(Tag.create("java"), Tag.create("プログラミング"));
        philosophyTags = List.of(Tag.create("アリストテレス"), Tag.create("哲学"));
    }

    @Test
    void findByKeyword_titleとtextの両方にkeywordを含む記事が返る() {
        articleRepository.save(Article.create("Java独習", "Javaができるには",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), javaTags));
        articleRepository.save(Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), philosophyTags));
        Page<Article> result = articleRepository.findByKeyword(
                "Java", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java独習");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByKeyword_titleにのみkeywordを含む記事が返る() {
        articleRepository.save(Article.create("Java独習", "初学者へのロードマップ",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), javaTags));
        articleRepository.save(Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), philosophyTags));
        Page<Article> result = articleRepository.findByKeyword(
                "Java", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java独習");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByKeyword_textにのみkeywordを含む記事が返る() {
        articleRepository.save(Article.create("プログラミング独習", "Java編",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), javaTags));
        articleRepository.save(Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), philosophyTags));
        Page<Article> result = articleRepository.findByKeyword(
                "Java", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getText()).isEqualTo("Java編");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByKeyword_titleにもtextにもkeywordを含まないなら空のPageが返る() {
        articleRepository.save(Article.create("プログラミング独習", "C#編",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), javaTags));
        articleRepository.save(Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), philosophyTags));
        Page<Article> result = articleRepository.findByKeyword(
                "Java", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findByKeyword_大文字小文字が異なっていてもkeywordを含む記事が返る() {
        articleRepository.save(Article.create("Java独習", "初学者へのロードマップ",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), javaTags));
        articleRepository.save(Article.create("アリストテレス入門", "アリストテレスとは",
                ArticleStatus.DRAFT, LocalDate.of(2026, 8, 1), philosophyTags));
        Page<Article> result = articleRepository.findByKeyword(
                "java", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java独習");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
