package com.leets_8th_be.kanghyeonwoo.report.adapter.out.user;

import com.leets_8th_be.kanghyeonwoo.report.application.port.out.ReporterDirectory;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import org.springframework.stereotype.Component;

@Component("reporterDirectory")
public class UserProfileAdapter implements ReporterDirectory {
    private final UserProfileLookup userProfiles;

    public UserProfileAdapter(UserProfileLookup userProfiles) {
        this.userProfiles = userProfiles;
    }

    @Override
    public boolean exists(UserId id) {
        return userProfiles.findById(id).isPresent();
    }
}
