package com.weolbu.assignment.member.application;

import com.weolbu.assignment.auth.JwtTokenManager;
import com.weolbu.assignment.auth.dto.JwtToken;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.repository.MemberRepository;
import com.weolbu.assignment.member.domain.MemberType;
import com.weolbu.assignment.member.dto.LoginRequest;
import com.weolbu.assignment.member.dto.MemberJoinRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.DUPLICATED_EMAIL;
import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.LOGIN_FAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenManager jwtTokenManager;

    @Test
    void 회원가입을_할_수_있다(){
        // given
        MemberJoinRequest request = createMemberJoinRequest();
        Member member = Member.join(request.withEncodedPassword("encodedPassword"));

        // when
        ReflectionTestUtils.setField(memberService, "salt", "salt");
        given(memberService.checkEmailExist(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password() + "salt")).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(member);

        memberService.join(request);

        // then
        verify(passwordEncoder, times(1)).encode(request.password() + "salt");
        verify(memberRepository).save(any(Member.class));

        assertThat(member.getEmail()).isEqualTo(request.email());

    }

    @Test
    void 이미_존재하는_이메일로_회원가입을_하면_실패한다(){
        // given
        MemberJoinRequest request = createMemberJoinRequest();

        given(memberService.checkEmailExist(request.email())).willReturn(true);

        // when / then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(DUPLICATED_EMAIL.getMessage());
    }

    @Test
    void 유효한_정보를_입력하면_로그인에_성공한다(){
        // given
        LoginRequest request = createLoginRequest();
        Member member = mock(Member.class);
        JwtToken jwtToken = new JwtToken("Bearer", "accessToken", "refreshToken");

        ReflectionTestUtils.setField(memberService, "salt", "salt");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password()+"salt", member.getPassword())).willReturn(true);
        given(jwtTokenManager.generateToken(any(UsernamePasswordAuthenticationToken.class))).willReturn(jwtToken);

        // when
        JwtToken response = memberService.login(request);

        // then
        assertThat(response).isEqualTo(jwtToken);
    }

    @Test
    void 유효하지_않은_이메일을_입력하면_로그인에_실패한다(){
        // given
        LoginRequest request = createLoginRequest();

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(LOGIN_FAIL.getMessage());
    }

    @Test
    void 유효하지_않은_비밀번호를_입력하면_로그인에_실패한다(){
        // given
        LoginRequest request = createLoginRequest();
        Member member = mock(Member.class);

        ReflectionTestUtils.setField(memberService, "salt", "salt");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password()+"salt", member.getPassword())).willReturn(false);

        // when / then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(LOGIN_FAIL.getMessage());
    }

    @Test
    void 회원_이메일이_존재히는지_확인할_수_있다() {
        // given
        String email = "test@email.com";
        given(memberRepository.existsByEmail(email)).willReturn(true);

        // when
        boolean result = memberService.checkEmailExist(email);

        // then
        assertThat(result).isTrue();
    }

    private MemberJoinRequest createMemberJoinRequest(){
        return new MemberJoinRequest(
                "학생1", "student_test@email.com", "010-1234-5678", "Password1", MemberType.STUDENT
        );
    }

    private LoginRequest createLoginRequest(){
        return new LoginRequest("student_test@email.com", "Password1");
    }

}