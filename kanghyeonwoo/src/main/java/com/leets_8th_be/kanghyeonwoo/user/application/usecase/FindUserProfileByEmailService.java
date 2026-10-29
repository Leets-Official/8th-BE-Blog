package com.leets_8th_be.kanghyeonwoo.user.application.usecase;
import com.leets_8th_be.kanghyeonwoo.user.application.model.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileEmailLookup;
import com.leets_8th_be.kanghyeonwoo.user.application.port.out.UserProfileStore;
import java.util.Optional;
public class FindUserProfileByEmailService implements UserProfileEmailLookup {
    private final UserProfileStore profiles;
    public FindUserProfileByEmailService(UserProfileStore profiles) { this.profiles = profiles; }
    @Override public Optional<UserProfile> findByEmail(String email) { return profiles.findByEmail(email); }
}
