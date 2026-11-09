package com.leets_8th_be.kanghyeonwoo.report.adapter.out.blog;

import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentTargetSnapshot;
import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentTargetType;
import com.leets_8th_be.kanghyeonwoo.blog.application.contract.ContentVisibility;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.ManageContentTargetsUseCase;
import com.leets_8th_be.kanghyeonwoo.report.application.model.ReportTargetSnapshot;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetKind;
import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReportTargetPort;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlogReportTargetAdapter implements ReportTargetPort {
    private final ManageContentTargetsUseCase blogTargets;

    public BlogReportTargetAdapter(ManageContentTargetsUseCase blogTargets) {
        this.blogTargets = blogTargets;
    }

    @Override
    public Optional<ReportTargetSnapshot> find(ReportTargetKind target, Long targetId) {
        return blogTargets
                .inspect(toBlogType(target), targetId)
                .map(BlogReportTargetAdapter::toReportSnapshot);
    }

    @Override
    public Optional<ReportTargetSnapshot> hide(ReportTargetKind target, Long targetId) {
        return blogTargets
                .hide(toBlogType(target), targetId)
                .map(BlogReportTargetAdapter::toReportSnapshot);
    }

    private static ContentTargetType toBlogType(ReportTargetKind target) {
        return switch (target) {
            case POST -> ContentTargetType.POST;
            case COMMENT -> ContentTargetType.COMMENT;
        };
    }

    private static ReportTargetSnapshot toReportSnapshot(ContentTargetSnapshot snapshot) {
        ContentVisibility parent = snapshot.parentPostVisibility();
        return new ReportTargetSnapshot(
                snapshot.authorId(),
                snapshot.title(),
                snapshot.content(),
                snapshot.visibility() == ContentVisibility.HIDDEN,
                snapshot.visibility() == ContentVisibility.DELETED,
                parent == null || parent == ContentVisibility.ACTIVE,
                parent == ContentVisibility.DELETED);
    }
}
