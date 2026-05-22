package com.weread.dto;

public class NoteItemDto {
    public enum Type { ANNOTATION, THOUGHT }

    private Type type;
    private String id;
    private String bookId;
    private String chapterTitle;
    private String markedText;
    private String content;
    private Long createdTime;

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }
    public String getMarkedText() { return markedText; }
    public void setMarkedText(String markedText) { this.markedText = markedText; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getCreatedTime() { return createdTime; }
    public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }
}
