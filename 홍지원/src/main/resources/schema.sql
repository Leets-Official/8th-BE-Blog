-- Existing tables are preserved. Schema changes require a separate migration.
CREATE TABLE IF NOT EXISTS `member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `age` TINYINT NULL,
    `email` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT '논리 삭제 시각 (NULL이면 미삭제)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `member_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT '논리 삭제 시각 (NULL이면 미삭제)',
    CONSTRAINT `FK_Member_TO_Post_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `member_id` BIGINT NOT NULL COMMENT '작성자 회원 ID',
    `post_id` BIGINT NOT NULL COMMENT '대상 게시글 ID',
    `parent_id` BIGINT NULL COMMENT '부모 댓글 ID (NULL이면 최상위 댓글)',
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL COMMENT '논리 삭제 시각 (NULL이면 미삭제)',
    CONSTRAINT `FK_Member_TO_Comment_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `FK_Post_TO_Comment_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
    CONSTRAINT `FK_Comment_TO_Comment_1` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `post_state` (
    `post_id` BIGINT NOT NULL PRIMARY KEY,
    `visibility` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `accepted_comment_id` BIGINT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL,
    UNIQUE KEY `uk_post_state_accepted_comment` (`accepted_comment_id`),
    CONSTRAINT `fk_post_state_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
    CONSTRAINT `fk_post_state_accepted_comment` FOREIGN KEY (`accepted_comment_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `reporter_id` BIGINT NOT NULL,
    `post_id` BIGINT NULL,
    `comment_id` BIGINT NULL,
    `reason` VARCHAR(1000) NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    `result` VARCHAR(1000) NULL,
    `resolved_by_id` BIGINT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted_at` DATETIME NULL,
    UNIQUE KEY `uk_report_reporter_post` (`reporter_id`, `post_id`),
    UNIQUE KEY `uk_report_reporter_comment` (`reporter_id`, `comment_id`),
    CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_report_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
    CONSTRAINT `fk_report_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`),
    CONSTRAINT `fk_report_resolved_by` FOREIGN KEY (`resolved_by_id`) REFERENCES `member` (`id`),
    CONSTRAINT `ck_report_single_target` CHECK (
        (`post_id` IS NOT NULL AND `comment_id` IS NULL) OR
        (`post_id` IS NULL AND `comment_id` IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
