
package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.entity.UserStatus;
import com.example.backend.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        // No Bearer token: continue through the security chain.
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            // Parse the token and validate its signature and expiration.
            Claims claims = jwtService.extractAllClaims(token);
            String email = claims.getSubject();

            // Do not replace an existing authentication.
            if (email != null
                    && !email.isBlank()
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                Optional<User> optionalUser =
                        userRepository.findByEmail(email);

                if (optionalUser.isPresent()) {

                    User user = optionalUser.get();

                    // Only approved users may authenticate.
                    if (user.getStatus() == UserStatus.APPROVED
                            && user.getRole() != null) {

                        var authorities = List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                )
                        );

                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        authorities
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }
            }

        } catch (JwtException | IllegalArgumentException exception) {

            // A malformed, expired, or invalid JWT cannot authenticate.
            SecurityContextHolder.clearContext();
        }

        // Spring Security will decide whether access is permitted.
        filterChain.doFilter(request, response);
    }
}
