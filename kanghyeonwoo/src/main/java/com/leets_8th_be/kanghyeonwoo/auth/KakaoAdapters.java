package com.leets_8th_be.kanghyeonwoo.auth;

import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Accounts;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Identity;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Kakao;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Ticket;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.Tickets;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService;
import com.leets_8th_be.kanghyeonwoo.auth.application.KakaoAuthenticationService;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoAdapters {
    @Bean
    Tickets authenticationTickets(OAuthTickets repository) {
        return new Tickets() {
            @Override
            public Optional<Ticket> lock(String hash) {
                return repository.lock(hash).map(KakaoAdapters::toTicket);
            }

            @Override
            public void save(Ticket ticket) {
                OAuthTicket row = repository.findById(ticket.hash).orElseGet(() -> createRow(ticket));
                row.kakaoId = ticket.kakaoId;
                row.email = ticket.email;
                row.userId = ticket.userId;
                repository.save(row);
            }

            @Override
            public void delete(Ticket ticket) {
                repository.deleteById(ticket.hash);
                repository.flush();
            }
        };
    }

    @Bean
    Kakao authenticationKakao(KakaoClient client) {
        return new Kakao() {
            @Override
            public String authorize(String state) {
                return client.authorize(state);
            }

            @Override
            public String frontend(String code) {
                return client.frontend(code);
            }

            @Override
            public Identity identity(String code) {
                var identity = client.identity(code);
                return new Identity(identity.id(), identity.email());
            }
        };
    }

    @Bean
    KakaoAuthenticationService kakaoAuthenticationService(
            Tickets tickets, Accounts accounts, AuthenticationService auth, Kakao kakao) {
        return new KakaoAuthenticationService(tickets, accounts, auth, kakao);
    }

    private static Ticket toTicket(OAuthTicket row) {
        Ticket ticket = new Ticket(row.hash, row.kind, row.browserHash, row.expiresAt);
        ticket.kakaoId = row.kakaoId;
        ticket.email = row.email;
        ticket.userId = row.userId;
        return ticket;
    }

    private static OAuthTicket createRow(Ticket ticket) {
        return new OAuthTicket(ticket.hash, ticket.kind, ticket.browserHash, ticket.expiresAt);
    }
}
