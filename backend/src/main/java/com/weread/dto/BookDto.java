package com.weread.dto;

public class BookDto {
    private String bookId;
    private String title;
    private String author;
    private String cover;
    private String intro;
    private Long lastReadTime;
    private Integer readStatus;
    private long annotationCount;
    private long thoughtCount;

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
    public long getAnnotationCount() { return annotationCount; }
    public void setAnnotationCount(long annotationCount) { this.annotationCount = annotationCount; }
    public long getThoughtCount() { return thoughtCount; }
    public void setThoughtCount(long thoughtCount) { this.thoughtCount = thoughtCount; }
}
