package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentPage;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.CommentView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CommentSearchQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.CommentStore;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.Comment;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.CommentState;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostState;
import com.leets_8th_be.kanghyeonwoo.shared.adapter.out.persistence.TextSearch;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCommentStore implements CommentStore {
    private final CommentJpaRepository comments;
    private final PostJpaRepository posts;

    public JpaCommentStore(CommentJpaRepository comments, PostJpaRepository posts) {
        this.comments = comments;
        this.posts = posts;
    }

    @Override
    public CommentPage search(CommentSearchQuery query) {
        var pageRequest =
                PageRequest.of(query.page() - 1, query.size(), Sort.by(Sort.Direction.DESC, "createdAt", "id"));
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
        return new CommentPage(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<CommentView> findTarget(CommentId id) {
        return comments.findById(id.value()).map(JpaCommentStore::toView);
    }

    @Override
    public Optional<Comment> findForUpdate(CommentId id) {
        return comments.findForUpdate(id.value()).map(JpaCommentStore::toDomain);
    }

    @Override
    public CommentView save(Comment comment) {
        CommentJpaEntity entity =
                comments.findById(comment.id().value()).orElseThrow(() -> new IllegalStateException("저장할 댓글이 없습니다."));
        entity.updateContent(comment.content());
        if (comment.state() == CommentState.HIDDEN) entity.hide();
        if (comment.deleted()) entity.delete();
        return toView(comments.save(entity));
    }

    @Override
    public CommentView create(PostId postId, Comment comment) {
        PostJpaEntity post = posts.getReferenceById(postId.value());
        CommentJpaEntity parent = comment.parentId() == null
                ? null
                : comments.getReferenceById(comment.parentId().value());
        return toView(comments.saveAndFlush(
                CommentJpaEntity.create(comment.content(), comment.authorId().value(), post, parent)));
    }

    private static CommentView toView(CommentJpaEntity entity) {
        Comment comment = toDomain(entity);
        return new CommentView(
                comment.id(),
                comment.authorId(),
                comment.parentId(),
                comment.content(),
                comment.displayContent(),
                comment.state(),
                comment.deleted(),
                new PostId(entity.getPostId()),
                PostState.valueOf(entity.getPostStatus().name()),
                entity.getCreatedAt());
    }

    private static Comment toDomain(CommentJpaEntity entity) {
        CommentState state =
                entity.getStatus() == CommentJpaEntity.Visibility.ACTIVE ? CommentState.ACTIVE : CommentState.HIDDEN;
        return new Comment(
                new CommentId(entity.getId()),
                new UserId(entity.getUserId()),
                entity.getParentId() == null ? null : new CommentId(entity.getParentId()),
                entity.getContent(),
                state,
                entity.isDeleted());
    }
}
