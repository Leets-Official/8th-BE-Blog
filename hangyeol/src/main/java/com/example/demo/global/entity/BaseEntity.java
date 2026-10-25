package com.example.demo.global.entity;

import com.example.demo.global.enums.IsPrivate;
import jakarta.persistence.*;
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
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IsPrivate isPrivate = IsPrivate.PUBLIC;

    public void changeVisibility(IsPrivate isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
