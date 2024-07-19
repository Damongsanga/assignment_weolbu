package com.weolbu.assignment.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weolbu.assignment.global.exception.errorcode.CustomErrorCode;
import com.weolbu.assignment.global.exception.response.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.NOT_VALID_USER;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(req, res);
        }
        catch (JwtException e) {
            makeErrorResponse(res);
        }
    }

    private void makeErrorResponse(HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setStatus(NOT_VALID_USER.getHttpStatus().value());
        res.getWriter().write(convertObjectToJson(ErrorResponse.builder()
                .code(NOT_VALID_USER.name())
                .message(NOT_VALID_USER.getMessage())
                .build()));
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
