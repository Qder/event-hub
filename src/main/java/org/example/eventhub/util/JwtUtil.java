package org.example.eventhub.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:eventHubSecretKeyForJwtTokenMustBeLongEnough1234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认 1 天，单位毫秒
    private long expiration;

    /** 生成签名密钥 */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 Token */
    public String generateToken(Long studentId, String studentNo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("studentId", studentId);
        claims.put("studentNo", studentNo);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(studentId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /** 解析 Token，返回 Claims（包含所有 payload） */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 Token 里取 studentId */
    public Long getStudentId(String token) {
        Object id = parseToken(token).get("studentId");
        return id == null ? null : Long.valueOf(id.toString());
    }

    /** 校验 Token 是否有效（签名正确 + 未过期） */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}