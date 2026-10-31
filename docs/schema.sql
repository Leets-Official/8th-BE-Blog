-- MySQL: matches Member, Post, Comment and BaseTimeEntity.
-- Timestamps are populated by JPA lifecycle callbacks.
CREATE DATABASE IF NOT EXISTS `leets`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `leets`;

CREATE TABLE IF NOT EXISTS `member` (
    `member_id` BIGINT NOT NULL AUTO_INCREMENT,
    `password` VARCHAR(100) NOT NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NULL,
    PRIMARY KEY (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `post` (
    `post_id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NULL,
    PRIMARY KEY (`post_id`),
    CONSTRAINT `fk_post_member` FOREIGN KEY (`member_id`)
        REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `comment` (
    `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `member_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NULL,
    PRIMARY KEY (`comment_id`),
    CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`)
        REFERENCES `post` (`post_id`),
    CONSTRAINT `fk_comment_member` FOREIGN KEY (`member_id`)
        REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
