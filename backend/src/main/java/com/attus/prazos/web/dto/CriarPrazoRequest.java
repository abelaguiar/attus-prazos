package com.attus.prazos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Dados para criacao de um prazo processual")
public record CriarPrazoRequest(

        @Schema(description = "Numero do processo vinculado ao prazo",
                example = "0001234-56.2026.8.26.0100", maxLength = 25)
        @NotBlank(message = "numeroProcesso e' obrigatorio")
        @Size(max = 25, message = "numeroProcesso deve ter no maximo 25 caracteres")
        String numeroProcesso,

        @Schema(description = "Descricao objetiva do ato ou compromisso",
                example = "Contestacao", maxLength = 255)
        @NotBlank(message = "descricao e' obrigatoria")
        @Size(max = 255, message = "descricao deve ter no maximo 255 caracteres")
        String descricao,

        @Schema(description = "Data limite do prazo", example = "2026-12-31")
        @NotNull(message = "dataPrazo e' obrigatoria")
        @FutureOrPresent(message = "dataPrazo nao pode estar no passado")
        LocalDate dataPrazo
) {
}
