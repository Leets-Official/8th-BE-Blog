package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

public interface DeletePostUseCase {
    PostId delete(DeletePostCommand command);
}
