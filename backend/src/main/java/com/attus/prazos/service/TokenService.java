package com.attus.prazos.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Emite o JWT assinado (HS256) a partir de uma autenticacao ja validada. */
@Service
public class TokenService {

    private static final String ISSUER = "attus-prazos";

    private final JwtEncoder encoder;
    private final Duration expiracao;

    public TokenService(JwtEncoder encoder,
            @Value("${app.jwt.expiration-seconds:3600}") long expiracaoSegundos) {
        this.encoder = encoder;
        this.expiracao = Duration.ofSeconds(expiracaoSegundos);
    }

    public String gerarToken(Authentication authentication) {
        Instant agora = Instant.now();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + " " + b)
                .orElse("");

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracao))
                .subject(authentication.getName())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpiracaoSegundos() {
        return expiracao.toSeconds();
    }
}
