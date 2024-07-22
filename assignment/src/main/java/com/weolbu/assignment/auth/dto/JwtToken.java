package com.weolbu.assignment.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record JwtToken(
         String grantType,
         String accessToken,
         String refreshToken
        ){

    public JwtToken withNoRefreshToken(){
        return new JwtToken(this.grantType, this.accessToken, null);
    }

}

