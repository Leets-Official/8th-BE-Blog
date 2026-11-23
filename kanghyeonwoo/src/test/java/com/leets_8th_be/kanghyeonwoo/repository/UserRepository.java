package com.leets_8th_be.kanghyeonwoo.repository;

import com.leets_8th_be.kanghyeonwoo.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
