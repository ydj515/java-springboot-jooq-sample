package org.example.javaspringbootjooqsample.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final JwtParser parser;
    private final long expirationMillis;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secretKey()));
        this.parser = Jwts.parserBuilder().setSigningKey(key).build();
        this.expirationMillis = jwtProperties.accessTokenExpiration();
    }

    public IssuedAccessToken issueAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        Instant now = Instant.now();

        return new IssuedAccessToken(
                AuthConstants.BEARER,
                createAccessToken(authentication.getName(), authorities, now),
                now.toEpochMilli() + expirationMillis
        );
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        String authorityClaim = claims.get(AuthConstants.AUTH_CLAIM, String.class);
        if (authorityClaim == null || authorityClaim.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("권한 정보가 없는 JWT 토큰입니다.");
        }

        List<SimpleGrantedAuthority> authorities = Arrays.stream(authorityClaim.split(","))
                .filter(value -> !value.isBlank())
                .map(SimpleGrantedAuthority::new)
                .toList();

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            parser.parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException exception) {
            throw new AuthenticationCredentialsNotFoundException("잘못된 JWT 서명입니다.", exception);
        } catch (ExpiredJwtException exception) {
            throw new JwtException("만료된 JWT 토큰입니다.", exception);
        } catch (UnsupportedJwtException exception) {
            throw new AuthenticationCredentialsNotFoundException("지원되지 않는 JWT 토큰입니다.", exception);
        } catch (IllegalArgumentException exception) {
            throw new AuthenticationCredentialsNotFoundException("JWT 토큰이 잘못되었습니다.", exception);
        }
    }

    private String createAccessToken(String subject, String authorities, Instant now) {
        return Jwts.builder()
                .setSubject(subject)
                .claim(AuthConstants.AUTH_CLAIM, authorities)
                .setExpiration(new Date(now.toEpochMilli() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return parser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException exception) {
            return exception.getClaims();
        }
    }

    public record IssuedAccessToken(
            String tokenType,
            String accessToken,
            long accessTokenExpiresAt
    ) {
    }
}
