package com.leets_8th_be.kanghyeonwoo.blog.application.port.out;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
public interface PostProjectionPort {
    void synchronize(PostView post);
    void remove(PostId postId);
    static PostProjectionPort noop() { return new PostProjectionPort() {
        @Override public void synchronize(PostView post) { }
        @Override public void remove(PostId postId) { }
    }; }
}
