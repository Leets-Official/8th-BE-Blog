package com.leets.mission.domain.user.repository;

import com.leets.mission.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
