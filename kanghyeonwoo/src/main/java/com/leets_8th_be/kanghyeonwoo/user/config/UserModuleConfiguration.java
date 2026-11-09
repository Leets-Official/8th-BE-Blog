package com.leets_8th_be.kanghyeonwoo.user.config;

import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import com.leets_8th_be.kanghyeonwoo.user.application.port.out.UserProfileStore;
import com.leets_8th_be.kanghyeonwoo.user.application.usecase.FindUserProfileService;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileEmailLookup;
import com.leets_8th_be.kanghyeonwoo.user.application.usecase.FindUserProfileByEmailService;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.CreateUserProfileUseCase;
import com.leets_8th_be.kanghyeonwoo.user.application.usecase.CreateUserProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserModuleConfiguration {
    @Bean
    public UserProfileLookup userProfileLookup(UserProfileStore profiles) {
        return new FindUserProfileService(profiles);
    }

    @Bean
    public UserProfileEmailLookup userProfileEmailLookup(UserProfileStore profiles) {
        return new FindUserProfileByEmailService(profiles);
    }

    @Bean
    public CreateUserProfileUseCase createUserProfileUseCase(UserProfileStore profiles) {
        return new CreateUserProfileService(profiles);
    }
}
