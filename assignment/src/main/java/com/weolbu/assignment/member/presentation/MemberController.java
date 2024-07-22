package com.weolbu.assignment.member.presentation;

import com.weolbu.assignment.auth.dto.JwtToken;
import com.weolbu.assignment.member.application.MemberService;
import com.weolbu.assignment.member.dto.LoginRequest;
import com.weolbu.assignment.member.dto.MemberJoinRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class MemberController {

    @Value("${spring.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(
            @Valid @RequestBody LoginRequest request
    ){
        JwtToken res = memberService.login(request);
        HttpHeaders headers = getHeadersWithCookie(res);
        return new ResponseEntity<>(res.withNoRefreshToken(), headers, HttpStatus.OK);
    }

    @PostMapping("/members")
    public ResponseEntity<Void> join(
            @Valid @RequestBody MemberJoinRequest request
    ){
        memberService.join(request);
        return ResponseEntity.status(CREATED).build();
    }

    private HttpHeaders getHeadersWithCookie(JwtToken jwtToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", jwtToken.refreshToken())
                .maxAge(refreshTokenValidityInSeconds)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", cookie.toString());
        return headers;
    }



}
