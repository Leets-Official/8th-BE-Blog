package com.leets.mission.domain.post.service;

import com.leets.mission.domain.comment.repository.CommentRepository;
import com.leets.mission.domain.post.converter.PostConverter;
import com.leets.mission.domain.post.dto.PostRequestDTO;
import com.leets.mission.domain.post.dto.PostResponseDTO;
import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.post.repository.PostRepository;
import com.leets.mission.domain.user.entity.User;
import com.leets.mission.domain.user.repository.UserRepository;
import com.leets.mission.global.apiPayload.code.ErrorStatus;
import com.leets.mission.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    // 게시글 생성
    @Transactional
    public PostResponseDTO.DetailDTO createPost(PostRequestDTO.CreateDTO request) {
        User user = findUser(request.userId());
        Post post = postRepository.save(PostConverter.toPost(request, user));
        return PostConverter.toDetailDTO(post);
    }

    // 게시글 목록 조회 (최신순)
    public PostResponseDTO.PreviewListDTO getPostList(int page, int size) {
        if (page < 0 || size < 1) {
            throw new GeneralException(ErrorStatus.POST_400_2);
        }
        // 작성 시각이 같으면 id가 큰 게시글이 먼저 오도록 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "id");
        Page<Post> postPage = postRepository.findAll(PageRequest.of(page, size, sort));
        return PostConverter.toPreviewListDTO(postPage);
    }

    // 게시글 조회
    public PostResponseDTO.DetailDTO getPost(Long postId) {
        return PostConverter.toDetailDTO(findPost(postId));
    }

    // 게시글 전체 수정 (제목과 본문을 한번에 모두교체)
    @Transactional
    public PostResponseDTO.DetailDTO updatePost(Long postId, PostRequestDTO.UpdateDTO request) {
        Post post = findPost(postId);
        //없으면 404
        if (!post.isWrittenBy(request.userId())) {
            //작성자 아니면 403
            throw new GeneralException(ErrorStatus.POST_403_1);
        }
        post.update(request.title(), request.content()); //엔티티에게 수정 시킴


        postRepository.flush();  //버그 수정
        return PostConverter.toDetailDTO(post);
    }

    // 게시글 부분 수정 (보낸 필드만 변경)
    @Transactional
    public PostResponseDTO.DetailDTO patchPost(Long postId, PostRequestDTO.PatchDTO request) {
        if (request.title() == null && request.content() == null) {
            throw new GeneralException(ErrorStatus.POST_400_1);
        }
        Post post = findPost(postId);
        if (!post.isWrittenBy(request.userId())) {
            throw new GeneralException(ErrorStatus.POST_403_1);
        }
        post.patch(request.title(), request.content());
        // 변경 내용을 DB에 즉시 반영해 응답에 갱신된 updatedAt이 담기도록 한다
        postRepository.flush();
        return PostConverter.toDetailDTO(post);
    }

    // 게시글 삭제
    @Transactional
    public PostResponseDTO.DeleteDTO deletePost(Long postId, Long userId) {
        Post post = findPost(postId); //404
        if (!post.isWrittenBy(userId)) {
            throw new GeneralException(ErrorStatus.POST_403_2);
        }
        // 댓글이 게시글을 참조하므로 댓글을 먼저 삭제
        commentRepository.deleteAllByPost(post);
        postRepository.delete(post);
        return PostConverter.toDeleteDTO(postId);
    }

    // 게시글 숨김 (ACTIVE → HIDDEN)
    @Transactional
    public PostResponseDTO.StatusDTO hidePost(Long postId, Long userId) {
        Post post = findPost(postId);
        if (!post.isWrittenBy(userId)) {
            throw new GeneralException(ErrorStatus.POST_403_3);
        }
        post.hide(); //이미 숨김이면 409
        return PostConverter.toStatusDTO(post);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_404_1));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_404_1));
    }
}
