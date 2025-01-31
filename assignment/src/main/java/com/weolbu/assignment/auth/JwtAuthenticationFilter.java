package com.weolbu.assignment.auth;

import com.weolbu.assignment.global.exception.BaseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.NO_REFRESH_TOKEN;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtTokenManager.resolveToken(request);
        String refreshToken = jwtTokenManager.resolveRefreshToken(request);

        log.info("getRequestURI : {}", request.getRequestURI());
        log.info("getRequestURL : {}", request.getRequestURL());

        if (accessToken != null){
            if (jwtTokenManager.validateToken(accessToken)) {
                this.setAuthentication(accessToken);
            }
            else if (refreshToken != null){
                if (jwtTokenManager.validateToken(refreshToken)){
                    String newAccessToken = jwtTokenManager.generateNewAccessToken(refreshToken);
                    response.setHeader("authorization", "bearer "+ accessToken);
                    this.setAuthentication(newAccessToken);
                }
            }
            else {
                throw new BaseException(NO_REFRESH_TOKEN);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        Authentication authentication = jwtTokenManager.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}