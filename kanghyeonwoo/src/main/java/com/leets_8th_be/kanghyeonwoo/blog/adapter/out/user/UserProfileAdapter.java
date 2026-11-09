package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.user;

import com.leets_8th_be.kanghyeonwoo.blog.application.port.out.AuthorDirectory;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;

import org.springframework.stereotype.Component;

@Component("blogAuthorDirectory")
public class UserProfileAdapter implements AuthorDirectory {
    private final UserProfileLookup userProfiles;

    public UserProfileAdapter(UserProfileLookup userProfiles) {
        this.userProfiles = userProfiles;
    }

    @Override
    public boolean exists(UserId id) {
        return userProfiles.findById(id).isPresent();
    }
}
