package com.leets.mission.domain.report.handler;

import com.leets.mission.domain.report.entity.ReportTargetType;

// 신고 대상별로 달라지는 동작 (새 신고 대상은 이 인터페이스의 구현체를 추가한다)
public interface ReportTargetHandler {

    // 이 핸들러가 담당하는 신고 대상 종류
    ReportTargetType getTargetType();

    // 신고 가능한 대상의 작성자 ID 조회 (없거나 숨김 처리된 대상이면 404)
    Long getActiveAuthorId(Long targetId);

    // 대상이 남아 있고 아직 노출 중일 때만 숨김 (이미 숨김/삭제된 대상은 그대로 둠)
    void hide(Long targetId);
}
