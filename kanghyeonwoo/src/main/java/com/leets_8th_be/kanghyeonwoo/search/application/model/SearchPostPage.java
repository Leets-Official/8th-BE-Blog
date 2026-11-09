package com.leets_8th_be.kanghyeonwoo.search.application.model;
import java.util.List;
public record SearchPostPage(List<SearchPost> items, int page, int size, long totalElements, int totalPages) {}
