package com.leets_8th_be.kanghyeonwoo.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import com.leets_8th_be.kanghyeonwoo.user.application.contract.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserProfileLookupTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileLookup userProfiles;

    @Test
    void returnsProfileWithoutExposingThePersistenceEntity() {
        String email = UUID.randomUUID() + "@example.com";
        User user = userRepository.save(User.create("프로필 사용자", email));

        UserProfile profile = userProfiles.findById(new UserId(user.getId())).orElseThrow();

        assertThat(profile.id()).isEqualTo(new UserId(user.getId()));
        assertThat(profile.username()).isEqualTo("프로필 사용자");
        assertThat(profile.email()).isEqualTo(email);
    }
}
