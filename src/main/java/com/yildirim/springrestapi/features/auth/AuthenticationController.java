package com.yildirim.springrestapi.features.auth;

import com.yildirim.springrestapi.features.auth.dto.JwtAuthenticationResponseDto;
import com.yildirim.springrestapi.features.auth.dto.UsernamePwdLoginDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authService;

    @PostMapping("/public/login")
    public ResponseEntity<JwtAuthenticationResponseDto> login(
            @RequestBody UsernamePwdLoginDto loginDto,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            throw new IllegalStateException("User is already authenticated");
        }

        return ResponseEntity.ok(authService.authenticate(loginDto, request, response));
    }

    @PostMapping("/public/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        authService.logout(request, response, authentication);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh-token")
    public JwtAuthenticationResponseDto refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setHeader("X-Access-Token", authService.refreshToken(request, response));

        return new JwtAuthenticationResponseDto(null);
    }
}
