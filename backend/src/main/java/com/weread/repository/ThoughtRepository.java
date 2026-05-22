package com.weread.repository;

import com.weread.entity.Thought;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThoughtRepository extends JpaRepository<Thought, Long> {
    Optional<Thought> findByThoughtId(String thoughtId);
    Page<Thought> findByBookIdOrderByCreatedTimeAsc(String bookId, Pageable pageable);
    List<Thought> findByBookIdOrderByCreatedTimeAsc(String bookId);
    long countByBookId(String bookId);
}
