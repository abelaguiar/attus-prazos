package com.attus.prazos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um novo usuario")
public record RegistrarRequest(

        @Schema(description = "Nome do usuario", example = "Maria Souza")
        @NotBlank(message = "nome e' obrigatorio")
        @Size(max = 120, message = "nome deve ter no maximo 120 caracteres")
        String nome,

        @Schema(description = "E-mail usado no login", example = "maria@exemplo.com")
        @NotBlank(message = "email e' obrigatorio")
        @Email(message = "email deve ser valido")
        @Size(max = 180, message = "email deve ter no maximo 180 caracteres")
        String email,

        @Schema(description = "Senha de acesso", example = "senhaForte123", minLength = 8)
        @NotBlank(message = "senha e' obrigatoria")
        @Size(min = 8, max = 100, message = "senha deve ter entre 8 e 100 caracteres")
        String senha
) {
}
