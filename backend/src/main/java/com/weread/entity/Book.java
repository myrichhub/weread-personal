package com.weread.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", unique = true, nullable = false, length = 100)
    private String bookId;

    @Column(length = 500)
    private String title;

    @Column(length = 500)
    private String author;

    @Column(length = 1000)
    private String cover;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(name = "last_read_time")
    private Long lastReadTime;

    @Column(name = "read_status")
    private Integer readStatus;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public Long getLastReadTime() { return lastReadTime; }
    public void setLastReadTime(Long lastReadTime) { this.lastReadTime = lastReadTime; }
    public Integer getReadStatus() { return readStatus; }
    public void setReadStatus(Integer readStatus) { this.readStatus = readStatus; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
