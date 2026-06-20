package com.attus.prazos.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Dados para atualizacao de um prazo processual")
public record AtualizarPrazoRequest(
        @Schema(
                        description = "Nova descricao objetiva do ato ou compromisso",
                        example = "Apelacao",
                        maxLength = 2000)
                @NotBlank(message = "descricao e' obrigatoria")
                @Size(max = 2000, message = "descricao deve ter no maximo 2000 caracteres")
                String descricao,
        @Schema(description = "Nova data limite do prazo", example = "2027-01-15")
                @NotNull(message = "dataPrazo e' obrigatoria")
                @FutureOrPresent(message = "dataPrazo nao pode estar no passado")
                LocalDate dataPrazo,
        @Schema(description = "Versao retornada pela ultima leitura do prazo", example = "0")
                @NotNull(message = "version e' obrigatoria")
                Long version) {}
