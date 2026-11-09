package com.example.demo.global;

/**
 * Swagger 에러 응답 예시 모음. (ErrorCode 의 code / message 와 같은 값으로 유지)
 */
public final class SwaggerErrorExamples {

    private SwaggerErrorExamples() {}

    // ---- 공통 입력값 오류 (COMMON_400_x) ----
    public static final String INVALID_BODY = """
            {"isSuccess": false, "code": "COMMON_400_1", "message": "올바르지 않은 입력값입니다.", "data": {"title": "제목을 입력해주세요."}}""";
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

    // ---- 회원 / 인증 ----
    public static final String INVALID_SIGNUP = """
            {"isSuccess": false, "code": "COMMON_400_1", "message": "올바르지 않은 입력값입니다.", "data": {"email": "올바른 이메일 형식으로 입력해주세요."}}""";
    public static final String INVALID_LOGIN = """
            {"isSuccess": false, "code": "COMMON_400_1", "message": "올바르지 않은 입력값입니다.", "data": {"email": "이메일을 입력해주세요."}}""";
    public static final String INVALID_REISSUE = """
            {"isSuccess": false, "code": "COMMON_400_1", "message": "올바르지 않은 입력값입니다.", "data": {"refreshToken": "refreshToken을 입력해주세요."}}""";
    public static final String PASSWORD_TOO_LONG = """
            {"isSuccess": false, "code": "AUTH_400_1", "message": "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.", "data": null}""";
    public static final String LOGIN_FAILED = """
            {"isSuccess": false, "code": "AUTH_401_1", "message": "이메일 또는 비밀번호가 올바르지 않습니다.", "data": null}""";
    public static final String AUTHENTICATION_REQUIRED = """
            {"isSuccess": false, "code": "AUTH_401_2", "message": "인증이 필요합니다. 유효한 Access Token을 보내주세요.", "data": null}""";
    public static final String INVALID_REFRESH_TOKEN = """
            {"isSuccess": false, "code": "AUTH_401_3", "message": "유효하지 않은 Refresh Token입니다.", "data": null}""";
    public static final String DUPLICATE_EMAIL = """
            {"isSuccess": false, "code": "USER_409_1", "message": "이미 가입된 이메일입니다.", "data": null}""";
    public static final String DUPLICATE_NICKNAME = """
            {"isSuccess": false, "code": "USER_409_2", "message": "이미 사용 중인 닉네임입니다.", "data": null}""";
}
