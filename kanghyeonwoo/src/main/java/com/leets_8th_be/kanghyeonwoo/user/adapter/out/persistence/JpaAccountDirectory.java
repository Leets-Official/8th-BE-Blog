package com.leets_8th_be.kanghyeonwoo.user.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.user.application.contract.Credentials;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;

import jakarta.persistence.EntityManager;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class JpaAccountDirectory implements AccountDirectory {
    private final EntityManager entityManager;

    public JpaAccountDirectory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Credentials view(UserProfileJpaEntity user) {
        return new Credentials(user.getId(), user.passwordHash(), user.kakaoId(), user.role());
    }

    public Optional<Credentials> byEmail(String email) {
        return entityManager
                .createQuery(
                        "from UserProfileJpaEntity where lower(trim(email))=:value",
                        UserProfileJpaEntity.class)
                .setParameter("value", email)
                .getResultStream()
                .findFirst()
                .map(this::view);
    }

    public Optional<Credentials> byKakaoId(String id) {
        return entityManager
                .createQuery(
                        "from UserProfileJpaEntity where kakaoId=:value",
                        UserProfileJpaEntity.class)
                .setParameter("value", id)
                .getResultStream()
                .findFirst()
                .map(this::view);
    }

    public Credentials byId(long id) {
        var user = entityManager.find(UserProfileJpaEntity.class, id);
        return user == null ? null : view(user);
    }

    public boolean emailExists(String email) {
        return byEmail(email).isPresent();
    }

    public Credentials register(String email, String nickname, String hash, String kakao) {
        if (emailExists(email) || nicknameExists(nickname)) {
            throw new DuplicateKeyException("이미 등록된 이메일 또는 닉네임입니다.");
        }
        var user = new UserProfileJpaEntity(nickname, email);
        user.credentials(hash, kakao);
        entityManager.persist(user);
        entityManager.flush();
        return view(user);
    }

    private boolean nicknameExists(String nickname) {
        return !entityManager
                .createQuery(
                        "select user.id from UserProfileJpaEntity user where"
                                + " trim(user.username)=:name",
                        Long.class)
                .setParameter("name", nickname)
                .getResultList()
                .isEmpty();
    }
}
