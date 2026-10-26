package com.leets_8th_be.kanghyeonwoo.blog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {
}
