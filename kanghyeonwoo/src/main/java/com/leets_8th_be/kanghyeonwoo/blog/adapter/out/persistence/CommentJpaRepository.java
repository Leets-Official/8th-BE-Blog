package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long>,
        JpaSpecificationExecutor<CommentJpaEntity> {
}
