package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;

public interface UpdatePostUseCase {
    PostView update(UpdatePostCommand command);
}
