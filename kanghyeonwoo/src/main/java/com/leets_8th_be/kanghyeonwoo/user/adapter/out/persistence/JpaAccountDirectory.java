package com.leets_8th_be.kanghyeonwoo.user.adapter.out.persistence;
import com.leets_8th_be.kanghyeonwoo.user.application.contract.Credentials;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Repository
@Transactional
public class JpaAccountDirectory implements AccountDirectory {
 private final EntityManager em;
 public JpaAccountDirectory(EntityManager em) { this.em=em; }
 private Credentials view(UserProfileJpaEntity u) { return new Credentials(u.getId(),u.passwordHash(),u.kakaoId(),u.role()); }
 public Optional<Credentials> byEmail(String email) { return em.createQuery("from UserProfileJpaEntity where lower(trim(email))=:value",UserProfileJpaEntity.class).setParameter("value",email).getResultStream().findFirst().map(this::view); }
 public Optional<Credentials> byKakaoId(String id) { return em.createQuery("from UserProfileJpaEntity where kakaoId=:value",UserProfileJpaEntity.class).setParameter("value",id).getResultStream().findFirst().map(this::view); }
 public Credentials byId(long id) { var u=em.find(UserProfileJpaEntity.class,id); return u==null ? null : view(u); }
 public boolean emailExists(String email) { return byEmail(email).isPresent(); }
 public Credentials register(String email,String nickname,String hash,String kakao) {
  if(emailExists(email) || !em.createQuery("select u.id from UserProfileJpaEntity u where trim(u.username)=:name",Long.class).setParameter("name",nickname).getResultList().isEmpty()) throw new org.springframework.dao.DuplicateKeyException("이미 등록된 이메일 또는 닉네임입니다.");
  var u=new UserProfileJpaEntity(nickname,email);u.credentials(hash,kakao);em.persist(u);em.flush();return view(u);
 }
}
