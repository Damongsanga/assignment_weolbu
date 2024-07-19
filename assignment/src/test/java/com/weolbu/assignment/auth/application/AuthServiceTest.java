package com.weolbu.assignment.auth.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void SecurityContextHolder에_등록되어_있는_회원의_정보를_찾아올_수_있다() {
        // given
        long memberId = 1L;
        given(authentication.getPrincipal()).willReturn(memberId);
        // when
        when(authService.findMemberId()).thenReturn(memberId);
        long result = authService.findMemberId();
        // then
        assertThat(result).isEqualTo(memberId);
    }

}