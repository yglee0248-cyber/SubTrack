package com.subtrack.global.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String HEADER_ALGORITHM = "HS256";
    private static final String HEADER_TYPE = "JWT";
    private static final TypeReference<Map<String, Object>> CLAIM_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms:3600000}")
    private long accessTokenExpirationMs;

    public String createAccessToken(Long memberId, String email, String role) {
        long now = Instant.now().getEpochSecond();
        long expiration = now + TimeUnit.MILLISECONDS.toSeconds(accessTokenExpirationMs);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", HEADER_ALGORITHM);
        header.put("typ", HEADER_TYPE);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", memberId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("iat", now);
        claims.put("exp", expiration);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(claims);
        String unsignedToken = encodedHeader + "." + encodedPayload;

        return unsignedToken + "." + createSignature(unsignedToken);
    }

    public String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    public boolean validateToken(String token) {
        try {
            Map<String, Object> claims = parseClaims(token);
            long expiration = getLongClaim(claims, "exp");
            return expiration > Instant.now().getEpochSecond();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Map<String, Object> claims = parseClaims(token);
        Long memberId = getLongClaim(claims, "sub");
        UserDetails userDetails = customUserDetailsService.loadUserByMemberId(memberId);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private Map<String, Object> parseClaims(String token) {
        String[] tokenParts = token.split("\\.");
        if (tokenParts.length != 3) {
            throw new IllegalArgumentException("JWT 형식이 올바르지 않습니다.");
        }

        validateHeader(tokenParts[0]);
        validateSignature(tokenParts);

        return readJson(tokenParts[1]);
    }

    private void validateHeader(String encodedHeader) {
        Map<String, Object> header = readJson(encodedHeader);

        if (!HEADER_ALGORITHM.equals(header.get("alg")) || !HEADER_TYPE.equals(header.get("typ"))) {
            throw new IllegalArgumentException("지원하지 않는 JWT 헤더입니다.");
        }
    }

    private void validateSignature(String[] tokenParts) {
        String unsignedToken = tokenParts[0] + "." + tokenParts[1];
        String expectedSignature = createSignature(unsignedToken);
        String actualSignature = tokenParts[2];

        boolean valid = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                actualSignature.getBytes(StandardCharsets.UTF_8)
        );

        if (!valid) {
            throw new IllegalArgumentException("JWT 서명이 올바르지 않습니다.");
        }
    }

    private String createSignature(String unsignedToken) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(keySpec);
            byte[] signature = mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 서명 생성에 실패했습니다.", exception);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("JWT JSON 인코딩에 실패했습니다.", exception);
        }
    }

    private Map<String, Object> readJson(String encodedValue) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedValue);
            return objectMapper.readValue(decoded, CLAIM_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT JSON 파싱에 실패했습니다.", exception);
        }
    }

    private Long getLongClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text) {
            return Long.parseLong(text);
        }

        throw new IllegalArgumentException("JWT claim 값을 읽을 수 없습니다. key=" + key);
    }
}
