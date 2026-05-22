package com.weread.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.weread.entity.Annotation;
import com.weread.entity.Book;
import com.weread.entity.Thought;
import com.weread.repository.AnnotationRepository;
import com.weread.repository.BookRepository;
import com.weread.repository.ThoughtRepository;
import com.weread.weread.WeReadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final WeReadClient weReadClient;
    private final BookRepository bookRepository;
    private final AnnotationRepository annotationRepository;
    private final ThoughtRepository thoughtRepository;

    public SyncService(WeReadClient weReadClient, BookRepository bookRepository,
                       AnnotationRepository annotationRepository, ThoughtRepository thoughtRepository) {
        this.weReadClient = weReadClient;
        this.bookRepository = bookRepository;
        this.annotationRepository = annotationRepository;
        this.thoughtRepository = thoughtRepository;
    }

    public enum SyncState { IDLE, RUNNING, DONE, FAILED }

    private final AtomicReference<SyncState> syncState = new AtomicReference<>(SyncState.IDLE);
    private volatile String syncMessage = "";

    @Async
    public void syncAll(String sessionCookie) {
        if (!syncState.compareAndSet(SyncState.IDLE, SyncState.RUNNING)
                && !syncState.compareAndSet(SyncState.DONE, SyncState.RUNNING)
                && !syncState.compareAndSet(SyncState.FAILED, SyncState.RUNNING)) {
            log.info("Sync already running");
            return;
        }
        try {
            syncMessage = "Syncing bookshelf...";
            List<String> bookIds = syncBookshelf(sessionCookie);
            syncMessage = "Syncing notes for " + bookIds.size() + " books...";
            for (String bookId : bookIds) {
                try {
                    syncAnnotations(bookId, sessionCookie);
                    syncThoughts(bookId, sessionCookie);
                } catch (Exception e) {
                    log.warn("Failed to sync notes for book {}: {}", bookId, e.getMessage());
                }
            }
            syncState.set(SyncState.DONE);
            syncMessage = "Done. " + bookIds.size() + " books synced.";
        } catch (Exception e) {
            log.error("Sync failed", e);
            syncState.set(SyncState.FAILED);
            syncMessage = "Failed: " + e.getMessage();
        }
    }

    private List<String> syncBookshelf(String sessionCookie) throws IOException {
        JsonNode data = weReadClient.fetchBookshelf(sessionCookie);
        List<String> bookIds = new ArrayList<>();
        JsonNode booksNode = data.path("books");
        if (!booksNode.isArray()) return bookIds;

        // Build bookId -> {updateTime, progress} from bookProgress array
        java.util.Map<String, JsonNode> progressMap = new java.util.HashMap<>();
        JsonNode progressArray = data.path("bookProgress");
        if (progressArray.isArray()) {
            for (JsonNode p : progressArray) {
                String bid = p.path("bookId").asText(null);
                if (bid != null) progressMap.put(bid, p);
            }
        }

        // Each item in "books" IS the book directly (no nested "book" key)
        for (JsonNode item : booksNode) {
            String bookId = item.path("bookId").asText(null);
            if (bookId == null || bookId.isBlank()) continue;

            Book book = bookRepository.findByBookId(bookId).orElse(new Book());
            book.setBookId(bookId);
            book.setTitle(item.path("title").asText(""));
            book.setAuthor(item.path("author").asText(""));
            book.setCover(item.path("cover").asText(""));
            book.setIntro(item.path("intro").asText(""));

            // Last read time: prefer bookProgress.updateTime, fall back to readUpdateTime
            JsonNode progress = progressMap.get(bookId);
            long lastReadTime = progress != null
                    ? progress.path("updateTime").asLong(0)
                    : item.path("readUpdateTime").asLong(0);
            book.setLastReadTime(lastReadTime);

            // Read status: 2=finished, 1=reading
            int progressPct = progress != null ? progress.path("progress").asInt(0) : 0;
            book.setReadStatus(progressPct >= 100 || item.path("finishReading").asInt(0) == 1 ? 2 : 1);
            book.setUpdatedAt(LocalDateTime.now());
            bookRepository.save(book);
            bookIds.add(bookId);
        }
        log.info("Synced {} books", bookIds.size());
        return bookIds;
    }

    private void syncAnnotations(String bookId, String sessionCookie) throws IOException {
        JsonNode data = weReadClient.fetchAnnotations(bookId, sessionCookie);
        int errCode = data.path("errCode").asInt(0);
        if (errCode != 0) {
            log.warn("Annotation API error for book {}: errCode={}, msg={}", bookId, errCode, data.path("errMsg").asText(""));
            return;
        }
        JsonNode items = data.path("updated");
        if (!items.isArray()) {
            log.debug("No 'updated' array for book {}, keys: {}", bookId, data.fieldNames());
            return;
        }

        for (JsonNode item : items) {
            String annotationId = item.path("bookmarkId").asText(null);
            if (annotationId == null || annotationId.isBlank()) continue;

            Annotation ann = annotationRepository.findByAnnotationId(annotationId).orElse(new Annotation());
            ann.setAnnotationId(annotationId);
            ann.setBookId(bookId);
            ann.setChapterUid(item.path("chapterUid").asText(""));
            ann.setChapterTitle(item.path("chapterTitle").asText(""));
            ann.setMarkedText(item.path("markText").asText(""));
            ann.setStyle(item.path("style").asInt(0));
            ann.setCreatedTime(item.path("createTime").asLong(0));
            if (ann.getCreatedAt() == null) ann.setCreatedAt(LocalDateTime.now());
            annotationRepository.save(ann);
        }
    }

    private void syncThoughts(String bookId, String sessionCookie) throws IOException {
        JsonNode data = weReadClient.fetchThoughts(bookId, sessionCookie);
        JsonNode items = data.path("reviews");
        if (!items.isArray()) return;

        for (JsonNode item : items) {
            // reviewId is at the item level, review details are nested under "review"
            String thoughtId = item.path("reviewId").asText(null);
            if (thoughtId == null || thoughtId.isBlank()) continue;

            JsonNode review = item.path("review");
            Thought thought = thoughtRepository.findByThoughtId(thoughtId).orElse(new Thought());
            thought.setThoughtId(thoughtId);
            thought.setBookId(bookId);
            thought.setChapterUid(String.valueOf(review.path("chapterUid").asInt(0)));
            thought.setChapterTitle(review.path("chapterName").asText("")); // API uses "chapterName"
            thought.setMarkedText(review.path("abstract").asText(""));
            thought.setContent(review.path("content").asText(""));
            thought.setCreatedTime(review.path("createTime").asLong(0));
            if (thought.getCreatedAt() == null) thought.setCreatedAt(LocalDateTime.now());
            thoughtRepository.save(thought);
        }
    }

    public SyncState getState() { return syncState.get(); }
    public String getMessage() { return syncMessage; }
    public void reset() { syncState.set(SyncState.IDLE); }
}
