package com.leets_8th_be.kanghyeonwoo.auth;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
interface OAuthTickets extends JpaRepository<OAuthTicket,String> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from OAuthTicket t where t.hash=:hash")
 Optional<OAuthTicket> lock(@Param("hash") String hash);
}
