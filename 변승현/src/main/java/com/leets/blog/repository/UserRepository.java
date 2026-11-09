package com.leets.blog.repository;

import com.leets.blog.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id and u.deletedAt is null")
    Optional<User> findActiveForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);
}
