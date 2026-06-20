package com.attus.prazos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso emitido apos o login")
public record TokenResponse(

        @Schema(description = "Token JWT a ser enviado no header Authorization",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Tempo de validade do token em segundos", example = "3600")
        long expiresIn
) {

    public static TokenResponse bearer(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
