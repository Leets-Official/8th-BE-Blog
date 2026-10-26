package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import com.leets_8th_be.kanghyeonwoo.util.TextSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCommentStore implements CommentStore {
    private final CommentJpaRepository comments;

    public JpaCommentStore(CommentJpaRepository comments) {
        this.comments = comments;
    }

    @Override
    public CommentPage search(CommentSearchQuery query) {
        var pageRequest = PageRequest.of(query.page() - 1, query.size(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Specification<CommentJpaEntity> criteria = (root, q, cb) ->
                cb.equal(root.get("post").get("id"), query.postId().value());
        if (query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = query.keyword().strip();
            criteria = criteria.and((root, q, cb) -> cb.and(
                    cb.equal(root.get("status"), CommentJpaEntity.Visibility.ACTIVE),
                    cb.isFalse(root.get("isDeleted")),
                    TextSearch.contains(cb, root.get("content"), keyword)));
        }

        Page<CommentView> result = comments.findAll(criteria, pageRequest).map(JpaCommentStore::toView);
        return new CommentPage(result.getContent(), result.getNumber() + 1, result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private static CommentView toView(CommentJpaEntity entity) {
        CommentState state = entity.getStatus() == CommentJpaEntity.Visibility.ACTIVE
                ? CommentState.ACTIVE : CommentState.HIDDEN;
        Comment comment = new Comment(new CommentId(entity.getId()), new UserId(entity.getUserId()),
                entity.getParentId() == null ? null : new CommentId(entity.getParentId()),
                entity.getContent(), state, entity.isDeleted());
        return new CommentView(comment, entity.getCreatedAt());
    }
}
