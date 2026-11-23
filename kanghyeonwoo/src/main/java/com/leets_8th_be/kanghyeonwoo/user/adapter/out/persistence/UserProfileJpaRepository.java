package com.leets_8th_be.kanghyeonwoo.user.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, Long> {
    Optional<UserProfileJpaEntity> findByEmail(String email);
}
