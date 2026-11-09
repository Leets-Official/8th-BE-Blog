package com.example.demo.global;

/**
 * Swagger 에러 응답 예시 모음. (ErrorCode 의 code / message 와 같은 값으로 유지)
 */
public final class SwaggerErrorExamples {

    private SwaggerErrorExamples() {}

    // ---- 공통 입력값 오류 (COMMON_400_x) ----
    public static final String INVALID_BODY = """
            {"isSuccess": false, "code": "COMMON_400_1", "message": "올바르지 않은 입력값입니다.", "data": {"title": "제목을 입력해주세요."}}""";
    public static final String INVALID_PARAM = """
            {"isSuccess": false, "code": "COMMON_400_1", "message": "올바르지 않은 입력값입니다.", "data": {"userId": "필수 값입니다."}}""";
    public static final String INVALID_PAGING = """
            {"isSuccess": false, "code": "COMMON_400_1", "message": "올바르지 않은 입력값입니다.", "data": {"size": "size는 1 이상의 숫자여야 합니다."}}""";
    public static final String NO_FIELD_TO_UPDATE = """
            {"isSuccess": false, "code": "COMMON_400_2", "message": "수정할 제목 또는 내용을 입력해주세요.", "data": null}""";

    // ---- 권한 ----
    public static final String FORBIDDEN_POST = """
            {"isSuccess": false, "code": "COMMON_403_1", "message": "게시글에 대한 권한이 없습니다.", "data": null}""";

    // ---- 게시글 ----
    public static final String POST_NOT_FOUND = """
            {"isSuccess": false, "code": "POST_404_1", "message": "존재하지 않는 게시글입니다.", "data": null}""";
    public static final String POST_ALREADY_HIDDEN = """
            {"isSuccess": false, "code": "POST_409_1", "message": "이미 숨김 처리된 게시글입니다.", "data": null}""";

    // ---- 사용자 ----
    public static final String USER_NOT_FOUND = """
            {"isSuccess": false, "code": "USER_404_1", "message": "존재하지 않는 사용자입니다.", "data": null}""";

    // ---- 신고 ----
    public static final String SELF_REPORT_NOT_ALLOWED = """
            {"isSuccess": false, "code": "REPORT_400_1", "message": "본인 게시글은 신고할 수 없습니다.", "data": null}""";
    public static final String REPORT_NOT_FOUND = """
            {"isSuccess": false, "code": "REPORT_404_1", "message": "존재하지 않는 신고입니다.", "data": null}""";
    public static final String ALREADY_REPORTED = """
            {"isSuccess": false, "code": "REPORT_409_1", "message": "이미 신고한 게시글입니다.", "data": null}""";
    public static final String REPORT_ALREADY_RESOLVED = """
            {"isSuccess": false, "code": "REPORT_409_2", "message": "이미 처리 완료된 신고입니다.", "data": null}""";

    // ---- 댓글 ----
    public static final String SELF_ACCEPT_NOT_ALLOWED = """
            {"isSuccess": false, "code": "COMMENT_400_1", "message": "본인 댓글은 채택할 수 없습니다.", "data": null}""";
    public static final String NOT_POST_OWNER = """
            {"isSuccess": false, "code": "COMMENT_403_1", "message": "게시글 작성자만 댓글을 채택할 수 있습니다.", "data": null}""";
    public static final String COMMENT_NOT_FOUND = """
            {"isSuccess": false, "code": "COMMENT_404_1", "message": "존재하지 않는 댓글입니다.", "data": null}""";
    public static final String COMMENT_ALREADY_ACCEPTED = """
            {"isSuccess": false, "code": "COMMENT_409_1", "message": "이미 채택된 댓글입니다.", "data": null}""";
    public static final String ANOTHER_COMMENT_ACCEPTED = """
            {"isSuccess": false, "code": "COMMENT_409_2", "message": "이미 채택된 댓글이 있는 게시글입니다.", "data": null}""";
}
