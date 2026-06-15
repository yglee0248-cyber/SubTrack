package com.subtrack.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms:3600000}")
    private long accessTokenExpirationMs;

    public String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    public boolean validateToken(String token) {
        // 실제 JWT 검증은 로그인 API 구현 단계에서 작성합니다.
        return false;
    }

    public Authentication getAuthentication(String token) {
        throw new IllegalStateException("JWT 인증 객체 생성은 로그인 API 구현 단계에서 작성합니다.");
    }
}
