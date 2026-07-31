package com.example.articles.repository;

import com.example.articles.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("""
            SELECT a
            FROM Article a
            WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Article> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
