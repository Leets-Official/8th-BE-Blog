package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PostJpaEntity p where p.id = :id")
    Optional<PostJpaEntity> findForUpdate(@Param("id") Long id);
}
