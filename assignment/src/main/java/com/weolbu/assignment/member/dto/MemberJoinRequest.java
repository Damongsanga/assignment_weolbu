package com.weolbu.assignment.member.dto;

import com.weolbu.assignment.member.domain.MemberType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MemberJoinRequest(
        @NotBlank
        String name,
        @Email
        String email,
        @Pattern(regexp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$", message = "올바른 전화번호 형식이어야 합니다.")
        String phoneNo,
        @Pattern(regexp = "^(?:(?=.*[a-z])(?=.*[A-Z])|(?=.*[a-z])(?=.*\\d)|(?=.*[A-Z])(?=.*\\d)).{6,10}$",
                message = "올바른 비밀번호 형식이어야 합니다. 6자 이상 10자 이하이며 영문 소문자, 대문자, 숫자 중 최소 두 가지 이상 조합이 필요합니다.")
        String password,
        @NotNull
        MemberType type
){
        public MemberJoinRequest withEncodedPassword(String encodedPassword) {
                return new MemberJoinRequest(
                        name, email, phoneNo, encodedPassword, type
                );
        }
}
