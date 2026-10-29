package com.leets_8th_be.kanghyeonwoo.blog.application.model;

import java.util.List;

public record CommentPage(List<CommentView> items, int page, int size, long totalElements, int totalPages) {
}
