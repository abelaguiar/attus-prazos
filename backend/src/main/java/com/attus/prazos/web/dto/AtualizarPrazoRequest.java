package com.attus.prazos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Dados para atualização de um prazo processual")
public record AtualizarPrazoRequest(

        @Schema(description = "Nova descrição objetiva do ato ou compromisso",
                example = "Apelação", maxLength = 2000)
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @Schema(description = "Nova data limite do prazo", example = "2027-01-15")
        @NotNull(message = "A data do prazo é obrigatória.")
        @FutureOrPresent(message = "A data do prazo não pode estar no passado.")
        LocalDate dataPrazo,

        @Schema(description = "Versão retornada pela última leitura do prazo",
                example = "0")
        @NotNull(message = "A versão é obrigatória.")
        Long version
) {
}
