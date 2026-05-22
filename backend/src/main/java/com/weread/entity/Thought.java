package com.weread.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "thoughts")
public class Thought {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "thought_id", unique = true, nullable = false, length = 100)
    private String thoughtId;

    @Column(name = "book_id", nullable = false, length = 100)
    private String bookId;

    @Column(name = "chapter_uid", length = 100)
    private String chapterUid;

    @Column(name = "chapter_title", length = 500)
    private String chapterTitle;

    @Column(name = "marked_text", columnDefinition = "TEXT")
    private String markedText;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public String getThoughtId() { return thoughtId; }
    public void setThoughtId(String thoughtId) { this.thoughtId = thoughtId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getChapterUid() { return chapterUid; }
    public void setChapterUid(String chapterUid) { this.chapterUid = chapterUid; }
    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }
    public String getMarkedText() { return markedText; }
    public void setMarkedText(String markedText) { this.markedText = markedText; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getCreatedTime() { return createdTime; }
    public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
