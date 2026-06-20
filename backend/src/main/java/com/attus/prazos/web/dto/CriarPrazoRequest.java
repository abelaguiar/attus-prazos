package com.attus.prazos.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Dados para criação de um prazo processual")
public record CriarPrazoRequest(

        @Schema(description = "Número do processo vinculado ao prazo",
                example = "0001234-56.2026.8.26.0100", maxLength = 25)
        @NotBlank(message = "O número do processo é obrigatório.")
        // O '?' final torna o padrão opcional de propósito: assim o campo vazio é reportado
        // apenas pelo @NotBlank, sem duplicar a mensagem com a do @Pattern.
        @Pattern(
                regexp = "(\\d{20}|\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4})?",
                message = "O número do processo deve ter os 20 dígitos do padrão CNJ.")
        @Size(max = 25, message = "O número do processo deve ter no máximo 25 caracteres.")
        String numeroProcesso,

        @Schema(description = "Descrição objetiva do ato ou compromisso",
                example = "Contestação", maxLength = 2000)
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @Schema(description = "Data limite do prazo", example = "2026-12-31")
        @NotNull(message = "A data do prazo é obrigatória.")
        @FutureOrPresent(message = "A data do prazo não pode estar no passado.")
        LocalDate dataPrazo
) {
}
