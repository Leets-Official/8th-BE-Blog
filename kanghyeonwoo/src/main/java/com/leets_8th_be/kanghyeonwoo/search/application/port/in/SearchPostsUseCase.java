package com.leets_8th_be.kanghyeonwoo.search.application.port.in;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchPostPage;
public interface SearchPostsUseCase { SearchPostPage search(int page, int size, String keyword); }
