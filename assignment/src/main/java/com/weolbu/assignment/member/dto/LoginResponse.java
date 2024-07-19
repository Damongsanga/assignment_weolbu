package com.weolbu.assignment.member.dto;

import com.weolbu.assignment.auth.dto.JwtToken;

public record LoginResponse (
        Long id,
        JwtToken jwtToken
){
    public LoginResponse withNoRefreshToken(){
        return new LoginResponse(id, jwtToken.deleteRefreshToken());
    }
}
