package org.example._thbe.domain.report.strategy;


import org.example._thbe.domain.report.enums.ReportContentType;

//전략 패턴 공통 인터페이스
public interface ReportStrategy {

    //타입 판단
    boolean isValidType(ReportContentType reportContentType);

    //신고 당한 유저 서치
    Long reportedUserId(Long domainId);

    //승인 도메인 처리
    void approve(Long postId);


}
