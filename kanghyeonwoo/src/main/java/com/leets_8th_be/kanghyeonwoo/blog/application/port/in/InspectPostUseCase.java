package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;

import java.util.Optional;

public interface InspectPostUseCase {
    Optional<PostView> inspect(PostId id);
}
