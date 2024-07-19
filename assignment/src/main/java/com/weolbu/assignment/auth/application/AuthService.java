package com.weolbu.assignment.auth.application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public Long findMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long)authentication.getPrincipal();
    }
}

