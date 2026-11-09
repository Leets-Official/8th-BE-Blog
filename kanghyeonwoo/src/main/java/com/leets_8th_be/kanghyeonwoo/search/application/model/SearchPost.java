package com.leets_8th_be.kanghyeonwoo.search.application.model;
import java.time.LocalDateTime;
public record SearchPost(long id, String title, long authorId, LocalDateTime createdAt) {}
