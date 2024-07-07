package com.yildirim.springrestapi.features.auth;

import com.yildirim.springrestapi.features.auth.token.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService jwtService;
    private final JpaUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Extract the JWT token from the header
        final String jwt = TokenService.extractAuthTokenFromHeader(request);
        final String username;

        if (jwt == null) {
            filterChain.doFilter(request, response); // Continue to the next filter
            return;
        }
        username = jwtService.extractUsername(jwt); // Extract the username from the JWT token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Check the database for the user with the extracted username
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // If the token is valid, set the authentication in the SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                // Set extra details about the request and the user, such as IP address
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.createEmptyContext().setAuthentication(authToken);
            }

        }
        filterChain.doFilter(request, response);
    }
}
