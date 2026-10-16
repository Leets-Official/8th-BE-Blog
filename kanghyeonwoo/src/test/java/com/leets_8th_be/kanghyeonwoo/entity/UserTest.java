package com.leets_8th_be.kanghyeonwoo.entity;

import com.leets_8th_be.kanghyeonwoo.config.JpaConfig;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaConfig.class)
class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("User를 정상적으로 생성하고 저장하면 ID와 생성/수정 시간이 자동 설정된다.")
    void createUserSuccess() {
        // given
        User user = User.builder()
                .username("wooddang")
                .email("wooddang@example.com")
                .build();

        // when
        User savedUser = userRepository.save(user);
        em.flush();
        em.clear();

        // then
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(foundUser.getId()).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("wooddang");
        assertThat(foundUser.getEmail()).isEqualTo("wooddang@example.com");
        assertThat(foundUser.getCreatedAt()).isNotNull();
        assertThat(foundUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("중복된 이메일로 User 생성 시 DataIntegrityViolationException 예외가 발생한다.")
    void duplicateEmailThrowsException() {
        // given
        User user1 = User.builder()
                .username("user1")
                .email("duplicate@example.com")
                .build();
        userRepository.save(user1);
        em.flush();

        User user2 = User.builder()
                .username("user2")
                .email("duplicate@example.com")
                .build();

        // when & then
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
