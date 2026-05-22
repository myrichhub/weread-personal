CREATE DATABASE IF NOT EXISTS weread DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weread;

CREATE TABLE IF NOT EXISTS books (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  book_id        VARCHAR(100) NOT NULL,
  title          VARCHAR(500),
  author         VARCHAR(500),
  cover          VARCHAR(1000),
  intro          TEXT,
  last_read_time BIGINT COMMENT 'Unix timestamp from WeRead',
  read_status    INT          COMMENT '1=reading 2=finished 3=abandoned',
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS annotations (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  annotation_id VARCHAR(100) NOT NULL,
  book_id       VARCHAR(100) NOT NULL,
  chapter_uid   VARCHAR(100),
  chapter_title VARCHAR(500),
  marked_text   TEXT,
  style         INT COMMENT 'highlight style from WeRead',
  created_time  BIGINT,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_annotation_id (annotation_id),
  INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS thoughts (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  thought_id  VARCHAR(100) NOT NULL,
  book_id     VARCHAR(100) NOT NULL,
  chapter_uid VARCHAR(100),
  chapter_title VARCHAR(500),
  marked_text TEXT,
  content     TEXT,
  created_time BIGINT,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_thought_id (thought_id),
  INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
