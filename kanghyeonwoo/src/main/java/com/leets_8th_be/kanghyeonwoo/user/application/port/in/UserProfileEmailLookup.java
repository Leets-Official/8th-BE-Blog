package com.leets_8th_be.kanghyeonwoo.user.application.port.in;
import com.leets_8th_be.kanghyeonwoo.user.application.model.UserProfile;
import java.util.Optional;
public interface UserProfileEmailLookup { Optional<UserProfile> findByEmail(String email); }
