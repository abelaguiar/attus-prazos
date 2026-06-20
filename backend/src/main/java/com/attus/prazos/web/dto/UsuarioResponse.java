package com.attus.prazos.web.dto;

import com.attus.prazos.domain.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados publicos de um usuario")
public record UsuarioResponse(

        @Schema(description = "Identificador do usuario", example = "1")
        Long id,

        @Schema(description = "Nome do usuario", example = "Maria Souza")
        String nome,

        @Schema(description = "E-mail do usuario", example = "maria@exemplo.com")
        String email
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
