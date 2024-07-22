package com.weolbu.assignment.auth;

import com.weolbu.assignment.auth.dto.JwtToken;
import com.weolbu.assignment.global.exception.BaseException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.weolbu.assignment.global.exception.errorcode.CustomErrorCode.NO_AUTHORIZATION;

@Slf4j
@Component
public class JwtTokenManager {

    private Key key;
    private long accessTokenValidityInSeconds;
    private long refreshTokenValidityInSeconds;

    public static final String HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer";

    @Autowired
    public JwtTokenManager(@Value("${spring.jwt.secret}") String secretKey,
                           @Value("${spring.jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
                           @Value("${spring.jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // base64로 디코딩 -> 바이트 배열로 변환
        this.key = Keys.hmacShaKeyFor(keyBytes); // hmacsha256으로 다시 암호화
        this.accessTokenValidityInSeconds = accessTokenValidityInSeconds;
        this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
    }

    public JwtToken generateToken(Authentication authentication) {

        Claims claims = Jwts.claims().setSubject(authentication.getName());
        // 권한 가져오기
        Set<String> types = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        claims.put("type", types);

        long now = (new Date()).getTime();

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setClaims(claims)
                .setExpiration(new Date(now + accessTokenValidityInSeconds * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setClaims(claims)
                .setExpiration(new Date(now + refreshTokenValidityInSeconds * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new JwtToken(TOKEN_PREFIX, accessToken, refreshToken);
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("type") == null) {
            throw new BaseException(NO_AUTHORIZATION, "권한 정보가 없는 토큰입니다.");
        }

        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) claims.get("type");

        Collection<? extends GrantedAuthority> authorities = types.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String accessToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);
            return true;
        }catch (ExpiredJwtException e) { // access token이 expire될 시에 바로 reissue로 redirect
            log.error("Expired JWT Token", e);
            return false;
        } catch (Exception e) {
            log.error("Invalid JWT Token", e);
            throw new JwtException("Invalid JWT Token");
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length()+1);
        }
        return null;
    }
    public String generateNewAccessToken(String refreshToken){
        Authentication authentication = this.getAuthentication(refreshToken);
        String memberId = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Date accessTokenExpiresIn = new Date((new Date()).getTime() + accessTokenValidityInSeconds * 1000);
        return Jwts.builder()
                .setSubject(memberId)
                .claim("auth", authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public String resolveRefreshToken(HttpServletRequest req){
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")){
                return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            }
        }
        return null;
    }


    // accessToken claim parsing (만료된 토큰도 복호화)
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}

