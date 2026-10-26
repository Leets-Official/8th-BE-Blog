package com.leets_8th_be.kanghyeonwoo.user.config;

import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import com.leets_8th_be.kanghyeonwoo.user.application.port.out.UserProfileStore;
import com.leets_8th_be.kanghyeonwoo.user.application.usecase.FindUserProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserModuleConfiguration {
    @Bean
    public UserProfileLookup userProfileLookup(UserProfileStore profiles) {
        return new FindUserProfileService(profiles);
    }
}
