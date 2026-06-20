package com.attus.prazos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Dados para criacao de um prazo processual")
public record CriarPrazoRequest(

        @Schema(description = "Numero do processo vinculado ao prazo",
                example = "0001234-56.2026.8.26.0100", maxLength = 25)
        @NotBlank(message = "numeroProcesso e' obrigatorio")
        @Pattern(
                regexp = "(\\d{20}|\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4})?",
                message = "numeroProcesso deve ter os 20 digitos do padrao CNJ")
        @Size(max = 25, message = "numeroProcesso deve ter no maximo 25 caracteres")
        String numeroProcesso,

        @Schema(description = "Descricao objetiva do ato ou compromisso",
                example = "Contestacao", maxLength = 2000)
        @NotBlank(message = "descricao e' obrigatoria")
        @Size(max = 2000, message = "descricao deve ter no maximo 2000 caracteres")
        String descricao,

        @Schema(description = "Data limite do prazo", example = "2026-12-31")
        @NotNull(message = "dataPrazo e' obrigatoria")
        @FutureOrPresent(message = "dataPrazo nao pode estar no passado")
        LocalDate dataPrazo
) {
}
