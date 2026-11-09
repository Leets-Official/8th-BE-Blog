package com.leets_8th_be.kanghyeonwoo.search.application.port.in;
import com.leets_8th_be.kanghyeonwoo.search.application.model.PostProjection;
public interface UpdatePostProjectionUseCase {
    void synchronize(PostProjection post);
    void remove(long postId);
}
