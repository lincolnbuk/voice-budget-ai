package com.lincolnbuk.voicebudgetai.service;

import com.lincolnbuk.voicebudgetai.dto.AuthResponse;
import com.lincolnbuk.voicebudgetai.dto.LoginRequest;
import com.lincolnbuk.voicebudgetai.dto.RegisterRequest;
import com.lincolnbuk.voicebudgetai.model.AppUser;
import com.lincolnbuk.voicebudgetai.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationSeconds;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
                       @Value("${app.jwt.issuer}") String issuer,
                       @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        if (users.existsByUsername(username)) throw new UsernameAlreadyExistsException();
        users.save(new AppUser(username, passwordEncoder.encode(request.password())));
        return issueToken(username);
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalize(request.username());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        return issueToken(username);
    }

    private AuthResponse issueToken(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(username)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", expirationSeconds);
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
