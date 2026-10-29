package com.leets_8th_be.kanghyeonwoo.user.application.usecase;
import com.leets_8th_be.kanghyeonwoo.user.application.model.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.CreateUserProfileUseCase;
import com.leets_8th_be.kanghyeonwoo.user.application.port.out.UserProfileStore;
public class CreateUserProfileService implements CreateUserProfileUseCase {
    private final UserProfileStore profiles;
    public CreateUserProfileService(UserProfileStore profiles) { this.profiles = profiles; }
    @Override public UserProfile create(String username, String email) { return profiles.create(username, email); }
}
