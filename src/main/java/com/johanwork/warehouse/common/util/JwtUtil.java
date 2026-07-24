package com.johanwork.warehouse.common.util;

import com.johanwork.warehouse.role.entity.Role;
import com.johanwork.warehouse.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil{

    private static final String SECRET = "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4";

    public String generateToken(Authentication authentication) {
        String jwtToken = null;
        var user = (User) authentication.getPrincipal();
        jwtToken = Jwts.builder()
                .issuer("JohanWork")
                .subject(user.getEmail())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining(", ")))
                .issuedAt(Date.from(Instant.now()))
                .expiration(new Date (System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey()).compact();
        return jwtToken;
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

}
