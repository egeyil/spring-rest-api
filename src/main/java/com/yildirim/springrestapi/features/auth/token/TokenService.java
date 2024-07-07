package com.yildirim.springrestapi.features.auth.token;

import com.yildirim.springrestapi.features.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class TokenService {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    //    @Value("${application.security.jwt.secret-key}")
    private static final SecretKey secretKey = Jwts.SIG.HS256.key()
            .build();
    //    @Value("${application.security.jwt.expiration}")
    private static final long jwtExpiration = 1000000;
    //    @Value("${application.security.jwt.refresh-token.expiration}")
    private static final long refreshExpiration = 10000000;
    private final ServerProperties serverProperties;
    private final TokenRepository tokenRepository;

    public static String extractAuthTokenFromHeader(HttpServletRequest request) {
        final String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || header.isBlank() || !header.startsWith(BEARER)) {
            return null;
        }

        return header.substring(BEARER.length());
    }

    public static String extractRefreshTokenFromCookies(HttpServletRequest request) {
        final var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (var cookie : cookies) {
            if (cookie.getName().equals(REFRESH_TOKEN_COOKIE)) {
                return cookie.getValue();
            }
        }

        return null;
    }


    public String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(expiration, ChronoUnit.MILLIS)))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public Cookie generateRefreshTokenCookie(
            UserDetails userDetails
    ) {
        String token = buildToken(new HashMap<>(), userDetails, refreshExpiration);
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, token);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge((int) refreshExpiration);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        return refreshCookie;
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void revokeToken(String token, boolean persist) {
        var storedToken = tokenRepository.findByTokenStr(token)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            if (persist) tokenRepository.save(storedToken);
        }
    }

    public void revokeToken(String token) {
        revokeToken(token, false);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(Instant.now()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public SecretKey getSecretKey() {
        //return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        return secretKey;
    }

    public Cookie generateExpiredRefreshTokenCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }
}
