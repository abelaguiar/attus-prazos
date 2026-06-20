package com.attus.prazos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para login")
public record LoginRequest(

        @Schema(description = "E-mail cadastrado", example = "maria@exemplo.com")
        @NotBlank(message = "email e' obrigatorio")
        String email,

        @Schema(description = "Senha de acesso", example = "senhaForte123")
        @NotBlank(message = "senha e' obrigatoria")
        String senha
) {
}
