package com.weread.service;

import com.weread.dto.BookDto;
import com.weread.dto.NoteItemDto;
import com.weread.dto.PageResponse;
import com.weread.entity.Annotation;
import com.weread.entity.Book;
import com.weread.entity.Thought;
import com.weread.repository.AnnotationRepository;
import com.weread.repository.BookRepository;
import com.weread.repository.ThoughtRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Shanghai"));

    private final BookRepository bookRepository;
    private final AnnotationRepository annotationRepository;
    private final ThoughtRepository thoughtRepository;

    public BookService(BookRepository bookRepository,
                       AnnotationRepository annotationRepository,
                       ThoughtRepository thoughtRepository) {
        this.bookRepository = bookRepository;
        this.annotationRepository = annotationRepository;
        this.thoughtRepository = thoughtRepository;
    }

    public PageResponse<BookDto> listBooks(int page, int size, String q, boolean hasNotes) {
        PageRequest pageable = PageRequest.of(page, size);
        boolean hasQ = q != null && !q.isBlank();

        Page<Book> p;
        if (hasQ && hasNotes) {
            p = bookRepository.findBooksWithNotesByQuery(q.trim(), pageable);
        } else if (hasNotes) {
            p = bookRepository.findBooksWithNotes(pageable);
        } else if (hasQ) {
            p = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrderByLastReadTimeDesc(
                    q.trim(), q.trim(), pageable);
        } else {
            p = bookRepository.findAllByOrderByLastReadTimeDesc(pageable);
        }

        List<BookDto> items = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageResponse<>(items, page, size, p.getTotalElements(), p.getTotalPages());
    }

    public BookDto getBook(String bookId) {
        return bookRepository.findByBookId(bookId).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));
    }

    public PageResponse<NoteItemDto> getNotes(String bookId, int page, int size) {
        List<NoteItemDto> all = new ArrayList<>();
        annotationRepository.findByBookIdOrderByCreatedTimeAsc(bookId).forEach(a -> all.add(toNoteDto(a)));
        thoughtRepository.findByBookIdOrderByCreatedTimeAsc(bookId).forEach(t -> all.add(toNoteDto(t)));
        all.sort(Comparator.comparingLong(n -> n.getCreatedTime() == null ? 0L : n.getCreatedTime()));

        int total = all.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int from = page * size;
        int to = Math.min(from + size, total);
        List<NoteItemDto> slice = from >= total ? List.of() : all.subList(from, to);
        return new PageResponse<>(slice, page, size, total, totalPages);
    }

    public String exportMarkdown(String bookId) {
        Book book = bookRepository.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));
        List<NoteItemDto> notes = new ArrayList<>();
        annotationRepository.findByBookIdOrderByCreatedTimeAsc(bookId).forEach(a -> notes.add(toNoteDto(a)));
        thoughtRepository.findByBookIdOrderByCreatedTimeAsc(bookId).forEach(t -> notes.add(toNoteDto(t)));
        notes.sort(Comparator.comparingLong(n -> n.getCreatedTime() == null ? 0L : n.getCreatedTime()));

        StringBuilder md = new StringBuilder();
        md.append("# ").append(book.getTitle()).append("\n\n");
        md.append("**作者：** ").append(book.getAuthor()).append("\n\n");
        if (book.getLastReadTime() != null && book.getLastReadTime() > 0) {
            String date = DATE_FMT.format(Instant.ofEpochSecond(book.getLastReadTime()));
            md.append("**最后阅读：** ").append(date).append("\n\n");
        }
        if (book.getIntro() != null && !book.getIntro().isBlank()) {
            md.append("## 内容简介\n\n").append(book.getIntro()).append("\n\n");
        }
        md.append("---\n\n## 标注与想法\n\n");

        String currentChapter = null;
        for (NoteItemDto note : notes) {
            String chapter = note.getChapterTitle();
            if (chapter != null && !chapter.equals(currentChapter)) {
                md.append("### ").append(chapter).append("\n\n");
                currentChapter = chapter;
            }
            if (note.getMarkedText() != null && !note.getMarkedText().isBlank()) {
                md.append("> ").append(note.getMarkedText().replace("\n", "\n> ")).append("\n\n");
            }
            if (note.getType() == NoteItemDto.Type.THOUGHT
                    && note.getContent() != null && !note.getContent().isBlank()) {
                md.append("💭 **想法：** ").append(note.getContent()).append("\n\n");
            }
        }
        return md.toString();
    }

    private BookDto toDto(Book b) {
        BookDto dto = new BookDto();
        dto.setBookId(b.getBookId());
        dto.setTitle(b.getTitle());
        dto.setAuthor(b.getAuthor());
        dto.setCover(b.getCover());
        dto.setIntro(b.getIntro());
        dto.setLastReadTime(b.getLastReadTime());
        dto.setReadStatus(b.getReadStatus());
        dto.setAnnotationCount(annotationRepository.countByBookId(b.getBookId()));
        dto.setThoughtCount(thoughtRepository.countByBookId(b.getBookId()));
        return dto;
    }

    private NoteItemDto toNoteDto(Annotation a) {
        NoteItemDto dto = new NoteItemDto();
        dto.setType(NoteItemDto.Type.ANNOTATION);
        dto.setId(a.getAnnotationId());
        dto.setBookId(a.getBookId());
        dto.setChapterTitle(a.getChapterTitle());
        dto.setMarkedText(a.getMarkedText());
        dto.setCreatedTime(a.getCreatedTime());
        return dto;
    }

    private NoteItemDto toNoteDto(Thought t) {
        NoteItemDto dto = new NoteItemDto();
        dto.setType(NoteItemDto.Type.THOUGHT);
        dto.setId(t.getThoughtId());
        dto.setBookId(t.getBookId());
        dto.setChapterTitle(t.getChapterTitle());
        dto.setMarkedText(t.getMarkedText());
        dto.setContent(t.getContent());
        dto.setCreatedTime(t.getCreatedTime());
        return dto;
    }
}
