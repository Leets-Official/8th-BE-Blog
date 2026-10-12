package com.leets.mission.domain.post.service;

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

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_404_1));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_404_1));
    }
}
