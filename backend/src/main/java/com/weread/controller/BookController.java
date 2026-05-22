package com.weread.controller;

import com.weread.dto.BookDto;
import com.weread.dto.NoteItemDto;
import com.weread.dto.PageResponse;
import com.weread.service.BookService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.listBooks(page, size));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> detail(@PathVariable String bookId) {
        return ResponseEntity.ok(bookService.getBook(bookId));
    }

    @GetMapping("/{bookId}/notes")
    public ResponseEntity<PageResponse<NoteItemDto>> notes(
            @PathVariable String bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookService.getNotes(bookId, page, size));
    }

    @GetMapping("/{bookId}/export")
    public ResponseEntity<byte[]> export(@PathVariable String bookId) {
        String md = bookService.exportMarkdown(bookId);
        byte[] bytes = md.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"weread-notes-" + bookId + ".md\"")
                .contentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"))
                .body(bytes);
    }
}
