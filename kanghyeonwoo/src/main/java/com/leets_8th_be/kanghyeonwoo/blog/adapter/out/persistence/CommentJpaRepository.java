package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CommentJpaRepository
        extends JpaRepository<CommentJpaEntity, Long>, JpaSpecificationExecutor<CommentJpaEntity> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CommentJpaEntity c where c.id = :id")
    Optional<CommentJpaEntity> findForUpdate(@Param("id") Long id);
}
