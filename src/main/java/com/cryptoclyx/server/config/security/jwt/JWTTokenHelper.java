package com.cryptoclyx.server.config.security.jwt;


import com.cryptoclyx.server.exceptions.InvalidTokenPairException;
import com.cryptoclyx.server.service.auth.UserService;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;


@Configuration
public class JWTTokenHelper {


    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    private static final String REFRESH_CLAIM = "refresh";
    private static final String NAME_CLAIM = "name";
    private final String secret;
    private final Long validityInMilliseconds;
    private final Long validityRefreshInMilliseconds;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JWTTokenHelper(UserService userDetailsService,
                          @Value("${jwt.auth.secret_key}") String secret,
                          @Value("${jwt.auth.expires_in_minutes:60}") Long expiredAccessMinutes,
                          @Value("${jwt.auth.expires_refresh_in_hours:24}") Long expiredRefreshHours) {
        this.userDetailsService = userDetailsService;
        this.secret = Base64.getEncoder().encodeToString(secret.getBytes());
        this.validityInMilliseconds = expiredAccessMinutes * 60 * 1_000;
        this.validityRefreshInMilliseconds = expiredRefreshHours * 60 * 60 * 1_000;
    }

    public Pair<String, String> createTokenPair(String userName, Collection<String> permissions) {
        final String accessToken = createAccessToken(userName, permissions);
        final String refreshToken = createRefreshToken(accessToken);
        return Pair.of(accessToken, refreshToken);
    }

    public Authentication getAuthentication(String username) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getLoginFromAccessToken(String accessToken) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(accessToken).getBody().getSubject();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, false);
    }

    public boolean validateRefreshToken(String accessToken, String refreshToken) {
        String signature = getSignature(accessToken);
        if (!validateToken(refreshToken, true)) {
            return false;
        }
        if (!Jwts.parser().setSigningKey(secret).parseClaimsJws(refreshToken).getBody().getSubject()
                .equals(signature)) {
            throw new InvalidTokenPairException("You've provided invalid pair accessToken - refreshToken.");
        }
        return true;
    }

    public String getEmailFromRefreshToken(String refreshToken) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(refreshToken).getBody().get(NAME_CLAIM, String.class);
    }

    public Long getExpireInSeconds() {
        return validityInMilliseconds / 1_000;
    }

    public String extractJwtFromRequest(HttpServletRequest request) {
        final String bearerString = AUTHORIZATION_PREFIX;
        final String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith(bearerString)) {
            return bearerToken.substring(bearerString.length());
        }
        return null;
    }

    public String createAccessToken(String userName, Collection<String> permissions) {
        final Claims claims = Jwts.claims().setSubject(userName);
        claims.put(REFRESH_CLAIM, false);
        claims.put("roles", Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));
        claims.put("permissions", permissions);
        final Date now = new Date();
        final Date validity = new Date(new Date().getTime() + validityInMilliseconds);
        return getJWT(claims, now, validity);
    }

    public String createRefreshToken(String accessToken) {
        String name = Jwts.parser().setSigningKey(secret).parseClaimsJws(accessToken).getBody().getSubject();
        String signature = getSignature(accessToken);
        final Claims claims = Jwts.claims().setSubject(signature);
        claims.put(NAME_CLAIM, name);
        claims.put(REFRESH_CLAIM, true);
        final Date now = new Date();
        final Date validity = new Date(new Date().getTime() + validityRefreshInMilliseconds);

        return getJWT(claims, now, validity);
    }

    private String getJWT(Claims claims, Date now, Date validity) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    private String getSignature(String accessToken) {
        String[] splitToken = accessToken.split("\\.");
        int signatureSectionNumberInToken = splitToken.length - 1;
        return splitToken[signatureSectionNumberInToken];
    }

    private boolean validateToken(String token, boolean isRefresh) {
        try {
            final Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            Boolean refreshClaim = (Boolean) claimsJws.getBody().get(REFRESH_CLAIM);
            return isRefresh == refreshClaim;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        }
    }
}
