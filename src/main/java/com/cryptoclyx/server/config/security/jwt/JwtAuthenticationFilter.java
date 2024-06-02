package com.cryptoclyx.server.config.security.jwt;

import com.cryptoclyx.server.exceptions.InvalidTokenPairException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTTokenHelper jwtTokenHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        val accessToken = jwtTokenHelper.extractJwtFromRequest(request);
        val refreshToken = request.getHeader("token");

        try {
            if (isRefreshToken(refreshToken, accessToken, request)) {
                String email = jwtTokenHelper.getEmailFromRefreshToken(refreshToken);
                authenticate(email);
                request.setAttribute("email", email);
            } else {
                if (StringUtils.hasText(accessToken) && jwtTokenHelper.validateAccessToken(accessToken)) {
                    String login = jwtTokenHelper.getLoginFromAccessToken(accessToken);
                    authenticate(login);
                }
            }
        } catch (ExpiredJwtException | BadCredentialsException | InvalidTokenPairException jwtException) {
            request.setAttribute("exception", jwtException);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isRefreshToken(String refreshToken, String accessToken, HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        return refreshToken != null
                && requestURL.contains("refresh")
                && jwtTokenHelper.validateRefreshToken(accessToken, refreshToken);
    }

    private static boolean authenticationIsRequired() {
        final Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        return existingAuth == null || !existingAuth.isAuthenticated() ||
                existingAuth instanceof AnonymousAuthenticationToken;
    }

    private void authenticate(String email) {
        Authentication authentication = jwtTokenHelper.getAuthentication(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}