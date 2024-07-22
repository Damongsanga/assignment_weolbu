package com.weolbu.assignment.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode{

    // Auth
    NO_AUTHORIZATION(HttpStatus.FORBIDDEN, "해당 기능에 대한 권한이 없는 사용자입니다"),
    NO_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "refresh token이 존재하지 않습니다"),
    NOT_VALID_USER(HttpStatus.UNAUTHORIZED, "사용자 권한이 유효하지 않습니다"),
    WRONG_ACCESS_WITHOUT_AUTHORIZATION(HttpStatus.FORBIDDEN, "비정상적인 접근입니다"),
    LOGIN_FAIL(HttpStatus.BAD_REQUEST, "아이디 혹은 비밀번호가 잘못되었습니다"),

    // Member
    NO_MEMBER(HttpStatus.BAD_REQUEST, "요청에 해당하는 사용자가 존재하지 않습니다"),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 해당 이메일로 가입된 유저가 존재합니다."),

    // Course
    DUPLICATED_TITLE(HttpStatus.CONFLICT, "이미 해당 강의명으로 등록된 강의가 있습니다."),
    NOT_INSTRUCTOR(HttpStatus.FORBIDDEN, "강사만 진행할 수 있습니다."),
    NO_COURSE(HttpStatus.NOT_FOUND, "해당하는 강의가 존재하지 않습니다."),
    SELF_ENROLLMENT(HttpStatus.BAD_REQUEST, "자신의 강의를 수강 신청할 수 없습니다."),
    FULL_COURSE(HttpStatus.BAD_REQUEST, "해당 강의의 수용인원을 초과하였습니다."),
    ALREADY_ENROLLED(HttpStatus.BAD_REQUEST, "이미 가입된 강의입니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
