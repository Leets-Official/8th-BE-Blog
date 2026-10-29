package com.leets_8th_be.kanghyeonwoo.blog.application.port.in;

import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentTargetSnapshot;
import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentTargetType;

import java.util.Optional;

public interface ManageContentTargetsUseCase {
    Optional<ContentTargetSnapshot> inspect(ContentTargetType target, Long targetId);

    Optional<ContentTargetSnapshot> hide(ContentTargetType target, Long targetId);
}
