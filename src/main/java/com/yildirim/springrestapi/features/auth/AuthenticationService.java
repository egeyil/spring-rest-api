package com.yildirim.springrestapi.features.auth;

import com.yildirim.springrestapi.features.auth.dto.JwtAuthenticationResponseDto;
import com.yildirim.springrestapi.features.auth.dto.UsernamePwdLoginDto;
import com.yildirim.springrestapi.features.auth.token.Token;
import com.yildirim.springrestapi.features.auth.token.TokenRepository;
import com.yildirim.springrestapi.features.auth.token.TokenService;
import com.yildirim.springrestapi.features.auth.token.TokenType;
import com.yildirim.springrestapi.features.user.User;
import com.yildirim.springrestapi.features.user.UserEvents;
import com.yildirim.springrestapi.features.user.UserRepository;
import com.yildirim.springrestapi.features.user.dto.UserResponseDto;
import com.yildirim.springrestapi.features.user.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthenticationService implements LogoutHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public boolean isValidPassword(String pwd) {
        if (pwd == null) {
            return false;
        }

        // Password must be at least 8 characters, contain at least one digit,
        // one upper case letter, one lower case letter, and one special character
        return pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[@#$%^&+=]).{8,20}$");
    }

    public String encodePwd(String pwd) {
        return passwordEncoder.encode(pwd);
    }


    public JwtAuthenticationResponseDto authenticate(UsernamePwdLoginDto loginDto,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {

        var authToken = new UsernamePasswordAuthenticationToken(
                loginDto.username(),
                loginDto.password()
        );

        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        Authentication authentication = authenticationManager.authenticate(authToken);

        var user = userRepository.getByUsername(loginDto.username())
                .orElseThrow(UserNotFoundException::new);

        SecurityContextHolder.createEmptyContext().setAuthentication(authentication);
        var refreshCookie = tokenService.generateRefreshTokenCookie((UserDetails) authentication.getPrincipal());
        var accessToken = tokenService.generateToken((UserDetails) authentication.getPrincipal());

        // Save the refresh token in the database and in the cookies
        response.addCookie(refreshCookie);
        saveUserToken(user, refreshCookie.getValue());

        response.setHeader("X-Access-Token", accessToken);

        return new JwtAuthenticationResponseDto(new UserResponseDto(user));
    }

    private void saveUserToken(User user, String jwtStr) {
        var token = Token.builder()
                .user(user)
                .tokenStr(jwtStr)
                .tokenType(TokenType.COOKIE)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public String refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        final String accessToken = TokenService.extractAuthTokenFromHeader(request);
        tokenService.revokeToken(accessToken, false);
        final String refreshToken = TokenService.extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }

        final String username = tokenService.extractUsername(refreshToken);
        if (username != null) {
            var user = this.userRepository.getByUsername(username)
                    .orElseThrow();
            SecurityUser securityUser = new SecurityUser(user);

            if (tokenService.isTokenValid(refreshToken, securityUser)) {
                return tokenService.generateToken(securityUser);
            }
        }
        return null;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String refreshToken = TokenService.extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            return;
        }
        response.addCookie(tokenService.generateExpiredRefreshTokenCookie());
        var storedToken = tokenRepository.findByTokenStr(refreshToken)
                .orElse(null);
        if (storedToken != null) {
            tokenService.revokeToken(storedToken.getTokenStr(), true);
            SecurityContextHolder.clearContext();
        }
    }


    @EventListener
    public void onPasswordChangedEvent(UserEvents.PasswordChangedEvent event) {
        var user = event.user();
        var storedTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (storedTokens.isEmpty()) {
            return;
        }
        storedTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(storedTokens);

        // Log out user
        SecurityContextHolder.clearContext();
    }
}
