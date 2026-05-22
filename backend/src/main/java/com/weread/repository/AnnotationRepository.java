package com.weread.repository;

import com.weread.entity.Annotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    Optional<Annotation> findByAnnotationId(String annotationId);
    Page<Annotation> findByBookIdOrderByCreatedTimeAsc(String bookId, Pageable pageable);
    List<Annotation> findByBookIdOrderByCreatedTimeAsc(String bookId);
    long countByBookId(String bookId);
}
