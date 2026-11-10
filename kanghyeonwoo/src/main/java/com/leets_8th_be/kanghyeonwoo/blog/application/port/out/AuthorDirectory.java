package com.leets_8th_be.kanghyeonwoo.blog.application.port.out;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public interface AuthorDirectory {
    boolean exists(UserId id);
}
