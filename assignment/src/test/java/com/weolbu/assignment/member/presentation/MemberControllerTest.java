package com.weolbu.assignment.member.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weolbu.assignment.auth.JwtAccessDeniedHandler;
import com.weolbu.assignment.auth.JwtAuthenticationEntryPoint;
import com.weolbu.assignment.auth.JwtTokenManager;
import com.weolbu.assignment.auth.config.SecurityConfig;
import com.weolbu.assignment.auth.dto.JwtToken;
import com.weolbu.assignment.global.exception.BaseException;
import com.weolbu.assignment.member.application.MemberService;
import com.weolbu.assignment.member.domain.Member;
import com.weolbu.assignment.member.domain.repository.MemberRepository;
import com.weolbu.assignment.member.domain.MemberType;
import com.weolbu.assignment.member.dto.LoginRequest;
import com.weolbu.assignment.member.dto.LoginResponse;
import com.weolbu.assignment.member.dto.MemberJoinRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.*;
import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.DUPLICATED_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
@TestInstance(value = PER_CLASS)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtTokenManager.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class MemberControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    MemberService memberService;
    @MockBean
    MemberRepository memberRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeAll
    public void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void 회원가입을_할_수_있다() throws Exception {

        // given
        MemberJoinRequest request = createMemberJoinRequest();
        Member member = Member.join(createMemberJoinRequest());

        given(memberService.join(request)).willReturn(1L);
        given(memberService.checkEmailExist(request.email())).willReturn(false);
        given(memberRepository.save(member)).willReturn(member);

        // when
        ResultActions result = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated());
    }

    @Test
    void 중복_이메일을_검증할_수_있다() throws Exception {

        // given
        MemberJoinRequest request = createMemberJoinRequest();
        BaseException baseException = new BaseException(DUPLICATED_EMAIL);

        willThrow(baseException).given(memberService).join(request);
        given(memberService.checkEmailExist(request.email())).willReturn(true);

        // when
        MvcResult mvcResult = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn();

        // then
        String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
        assertThat(message).isEqualTo(baseException.getMessage());
    }

    @Test
    void 비밀번호_조건을_검사할_수_있다() throws Exception{

        // given
        MemberJoinRequest onlyUpper = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "PASSWORD", MemberType.STUDENT
        );
        MemberJoinRequest onlyLower = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "password", MemberType.STUDENT
        );
        MemberJoinRequest onlyNumeral = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "11111111", MemberType.STUDENT
        );
        MemberJoinRequest lessThan6 = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "Passw", MemberType.STUDENT
        );
        MemberJoinRequest moreThen10 = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "Password123", MemberType.STUDENT
        );

        List<MemberJoinRequest> wrongRequests = List.of(onlyUpper, onlyLower, onlyNumeral, moreThen10, lessThan6);

        MemberJoinRequest lengthOf6 = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "Passwo", MemberType.STUDENT
        );
        MemberJoinRequest lengthOf10 = new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "Password12", MemberType.STUDENT
        );

        List<MemberJoinRequest> validRequest = List.of(lengthOf6, lengthOf10);

        given(memberService.join(lengthOf6)).willReturn(1L);
        given(memberService.join(lengthOf10)).willReturn(1L);

        // when / then
        for(MemberJoinRequest req : wrongRequests){
            mockMvc.perform(post("/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        for(MemberJoinRequest req : validRequest){
            mockMvc.perform(post("/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

    }

    @Test
    void 유효한_회원_정보를_입력하면_로그인에_성공한다() throws Exception {
        // given
        LoginRequest request = createLoginRequest();
        JwtToken jwtToken = new JwtToken("Bearer", "accessTokenGenerated", "refreshTokenGenerated");
        LoginResponse response = new LoginResponse(
                1L, jwtToken
        );
        Member member = Member.join(createMemberJoinRequest());

        given(memberService.login(request)).willReturn(response);
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(true);

        // when
        MvcResult mvcResult = mockMvc.perform(post( "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        LoginResponse responseFromMvc = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), LoginResponse.class);
        Cookie cookie = mvcResult.getResponse().getCookie("refreshToken");
        assert cookie != null;

        assertThat(responseFromMvc.jwtToken().accessToken()).isEqualTo("accessTokenGenerated");
        assertThat(responseFromMvc.jwtToken().refreshToken()).isNull();
        assertThat(cookie.getValue()).isEqualTo("refreshTokenGenerated");

    }

    @Test
    void 유효하지_않은_이메일을_입력하면_로그인에_실패한다() throws Exception {
        // given
        LoginRequest request = createLoginRequest();
        BaseException baseException = new BaseException(LOGIN_FAIL);

        willThrow(baseException).given(memberService).login(request);
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());

        // when
        MvcResult mvcResult = mockMvc.perform(post( "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
        assertThat(message).isEqualTo(baseException.getMessage());
    }

    @Test
    void 유효하지_않은_비밀번호_입력하면_로그인에_실패한다() throws Exception {
        // given
        LoginRequest request = createLoginRequest();
        BaseException baseException = new BaseException(LOGIN_FAIL);
        Member member = Member.join(createMemberJoinRequest());

        willThrow(baseException).given(memberService).login(request);
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(false);

        // when
        MvcResult mvcResult = mockMvc.perform(post( "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
        assertThat(message).isEqualTo(baseException.getMessage());
    }


    private MemberJoinRequest createMemberJoinRequest(){
        return new MemberJoinRequest(
                "학생1", "student@email.com", "010-1234-5678", "Password1", MemberType.STUDENT
        );
    }

    private LoginRequest createLoginRequest(){
        return new LoginRequest("student@email.com", "Password1");
    }


}