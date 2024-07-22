package com.weolbu.assignment.member.dto;

import jakarta.validation.constraints.*;

public record LoginRequest (
        @Email
        String email,
        @Pattern(regexp = "^(?:(?=.*[a-z])(?=.*[A-Z])|(?=.*[a-z])(?=.*\\d)|(?=.*[A-Z])(?=.*\\d)).{6,10}$",
                message = "올바른 비밀번호 형식이어야 합니다. 6자 이상 10자 이하이며 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상 조합이 필요합니다.")
        String password
){
}
