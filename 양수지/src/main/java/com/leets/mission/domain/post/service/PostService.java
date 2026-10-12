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

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_404_1));
    }
}
