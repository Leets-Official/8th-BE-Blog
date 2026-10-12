package com.leets.mission.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * [확장성 설계] Soft Delete(논리 삭제)를 위한 삭제 시각 저장 필드.
     * - 현 단계: 필드 선언 및 softDelete() 상태 변경 메서드만 구현.
     * - 추후 구현: Entity 상단에 @SQLDelete 및 @SQLRestriction 어노테이션을 부착하여 자동 삭제/조회 필터링 적용 예정.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}