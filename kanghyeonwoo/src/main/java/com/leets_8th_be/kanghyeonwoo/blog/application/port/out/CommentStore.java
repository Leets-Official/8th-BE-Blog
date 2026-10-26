package com.leets_8th_be.kanghyeonwoo.blog.application.port.out;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;

public interface CommentStore {
    CommentPage search(CommentSearchQuery query);
}
