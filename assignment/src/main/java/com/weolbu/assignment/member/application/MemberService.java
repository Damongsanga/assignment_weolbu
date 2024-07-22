package com.weolbu.assignment.member.application;

import com.weolbu.assignment.auth.JwtTokenManager;
import com.weolbu.assignment.auth.dto.JwtToken;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.repository.MemberRepository;
import com.weolbu.assignment.member.dto.LoginRequest;
import com.weolbu.assignment.member.dto.MemberJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenManager jwtTokenManager;
    private final PasswordEncoder passwordEncoder;

    @Value("spring.security.password.salt")
    private String salt;

    @Transactional
    public Long join(MemberJoinRequest request){

        if (checkEmailExist(request.email()))
            throw new BaseException(DUPLICATED_EMAIL);

        String encodedPassword = passwordEncoder.encode(request.password() + salt);
        Member member = Member.join(request.withEncodedPassword(encodedPassword));
        memberRepository.save(member);

        return member.getId();
    }

    public boolean checkEmailExist(String email) {
        return memberRepository.existsByEmail(email);
    }

    public JwtToken login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(() -> new BaseException(LOGIN_FAIL));
        authenticateLoginRequest(request, member);

        return createJwtToken(member);
    }

    public Member findById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NO_MEMBER));
    }

    private void authenticateLoginRequest(LoginRequest req, Member member) {
        if (!passwordEncoder.matches(req.password()+ salt, member.getPassword()))
            throw new BaseException(LOGIN_FAIL);
    }

    private JwtToken createJwtToken(Member member) {
        return jwtTokenManager.generateToken(new UsernamePasswordAuthenticationToken(
                member.getId().toString(), null, member.getAuthorities()));
    }
}
