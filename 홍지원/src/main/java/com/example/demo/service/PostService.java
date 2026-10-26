package com.example.demo.service;

import com.example.demo.controller.ApiExceptionHandler.ApiException;
import com.example.demo.domain.member.Member;
import com.example.demo.domain.member.MemberRepository;
import com.example.demo.domain.post.Post;
import com.example.demo.domain.post.PostRepository;
import com.example.demo.dto.PostDto;
import java.nio.charset.StandardCharsets;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostService(PostRepository postRepository, MemberRepository memberRepository) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    public PostDto.PostPage list(int page, int size, String sort) {
        if (page < 1) throw new ApiException(400, "INVALID_PAGE_NUMBER", "페이지 번호는 1 이상이어야 합니다.");
        if (size < 1 || size > 50) throw new ApiException(400, "INVALID_PAGE_SIZE", "페이지 크기는 1~50이어야 합니다.");
        Sort order = switch (sort) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt", "postId");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt", "postId");
            case "popular" -> Sort.by(Sort.Direction.DESC, "commentCount", "createdAt", "postId");
            default -> throw new ApiException(400, "INVALID_SORT_TYPE", "지원하지 않는 정렬 기준입니다.");
        };
        var result = postRepository.findActivePosts(PageRequest.of(page - 1, size, order));
        var posts = result.getContent().stream().map(p -> new PostDto.Summary(
                p.getPostId(), p.getTitle(), p.getAuthor(), p.getCreatedAt(), p.getCommentCount())).toList();
        return new PostDto.PostPage(posts, page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }

    public PostDto.Detail detail(long postId) {
        if (postId < 1) throw new ApiException(400, "INVALID_POST_ID", "게시글 ID는 1 이상이어야 합니다.");
        Post post = postRepository.findVisibleById(postId).orElseThrow(
                () -> new ApiException(404, "POST_NOT_FOUND", "해당 게시글을 찾을 수 없습니다."));
        return PostDto.Detail.from(post);
    }

    @Transactional
    public PostDto.Saved create(String userId, PostDto.Request request) {
        Member member = currentMember(userId);
        validate(request);
        Post post = postRepository.saveAndFlush(new Post(member, request.title(), request.content()));
        return PostDto.Saved.from(post);
    }

    @Transactional
    public PostDto.Saved update(long postId, String userId, PostDto.Request request) {
        Member member = currentMember(userId);
        Post post = findPost(postId);
        checkAuthor(post, member);
        validate(request);
        post.update(request.title(), request.content());
        return PostDto.Saved.from(post);
    }

    @Transactional
    public PostDto.Deleted delete(long postId, String userId) {
        Member member = currentMember(userId);
        Post post = findPost(postId);
        checkAuthor(post, member);
        post.delete();
        return new PostDto.Deleted(post.getId());
    }

    private Post findPost(long id) {
        if (id < 1) throw new ApiException(400, "INVALID_POST_ID", "게시글 ID는 1 이상이어야 합니다.");
        return postRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new ApiException(404, "POST_NOT_FOUND", "해당 게시글을 찾을 수 없습니다."));
    }

    // 과제용 사용자 전달 방식이며 실제 인증이 아닙니다.
    private Member currentMember(String userId) {
        long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException exception) {
            throw new ApiException(401, "UNAUTHORIZED", "유효한 사용자 ID가 필요합니다.");
        }
        return memberRepository.findById(id).filter(m -> m.getDeletedAt() == null).orElseThrow(
                () -> new ApiException(401, "UNAUTHORIZED", "유효한 사용자 ID가 필요합니다."));
    }

    private void checkAuthor(Post post, Member member) {
        if (!post.getMember().getId().equals(member.getId())) {
            throw new ApiException(403, "POST_ACCESS_DENIED", "게시글 작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private void validate(PostDto.Request request) {
        if (request == null || request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new ApiException(400, "REQUIRED_FIELD_MISSING", "제목과 내용은 필수이며 공백만 입력할 수 없습니다.");
        }
        if (request.title().length() > 100) {
            throw new ApiException(400, "TITLE_LENGTH_EXCEEDED", "제목은 100자 이하여야 합니다.");
        }
        if (request.content().getBytes(StandardCharsets.UTF_8).length > 65_535) {
            throw new ApiException(400, "CONTENT_LENGTH_EXCEEDED",
                    "본문은 UTF-8 기준 65,535바이트 이하여야 합니다.");
        }
    }
}
