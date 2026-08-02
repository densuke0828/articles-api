package com.example.articles.entity;

import com.example.articles.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @BatchSize(size = 10)
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

     public static Article create(String title, String text, ArticleStatus status,
                                  LocalDate publicationDate, List<Tag> tags) {
         Article article = Article.builder()
                 .title(title)
                 .text(text)
                 .status(status)
                 .publicationDate(publicationDate)
                 .build();
         tags.forEach(article::addTag);
         return article;
     }

     public void addTag(Tag tag) {
         this.tags.add(tag);
         tag.assignArticle(this);
     }

     public void removeTag(Tag tag) {
         this.tags.remove(tag);
         tag.unassignArticle(this);
     }

    public void update(String title, String text, ArticleStatus status,
                       LocalDate publicationDate, List<Tag> tags) {
        this.title = title;
        this.text = text;
        this.status = status;
        this.publicationDate = publicationDate;
        new ArrayList<>(this.tags).forEach(this::removeTag);
        tags.forEach(this::addTag);
    }
}
