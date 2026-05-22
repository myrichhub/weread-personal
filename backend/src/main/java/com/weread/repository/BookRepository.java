package com.weread.repository;

import com.weread.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookId(String bookId);
    Page<Book> findAllByOrderByLastReadTimeDesc(Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrderByLastReadTimeDesc(
            String title, String author, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
           "(EXISTS (SELECT 1 FROM Annotation a WHERE a.bookId = b.bookId) " +
           "OR EXISTS (SELECT 1 FROM Thought t WHERE t.bookId = b.bookId)) " +
           "ORDER BY b.lastReadTime DESC")
    Page<Book> findBooksWithNotes(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (EXISTS (SELECT 1 FROM Annotation a WHERE a.bookId = b.bookId) " +
           "OR EXISTS (SELECT 1 FROM Thought t WHERE t.bookId = b.bookId)) " +
           "ORDER BY b.lastReadTime DESC")
    Page<Book> findBooksWithNotesByQuery(@Param("q") String q, Pageable pageable);
}
