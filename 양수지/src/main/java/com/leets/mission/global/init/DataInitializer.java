package com.leets.mission.global.init;

import com.leets.mission.domain.user.entity.User;
import com.leets.mission.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// 서버 시작 시 테스트용 사용자 2명 생ㅅ어
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        User tester1 = userRepository.save(User.builder().nickname("tester1").build());
        User tester2 = userRepository.save(User.builder().nickname("tester2").build());
        log.info("테스트 사용자 생성 완료: userId {} = {}, userId {} = {}",
                tester1.getId(), tester1.getNickname(), tester2.getId(), tester2.getNickname());
    }
}
