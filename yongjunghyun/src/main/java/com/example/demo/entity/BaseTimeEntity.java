package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 모든 Entity가 공통으로 갖는 작성/수정 시각.
 * @MappedSuperclass: 이 클래스 자체는 테이블이 되지 않고,
 * 상속받은 Entity의 테이블에 created_at, updated_at 컬럼만 물려준다.
 */
@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}