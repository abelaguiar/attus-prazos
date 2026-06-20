package com.attus.prazos.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Erro padronizado retornado pela API")
public record ApiError(
        @Schema(description = "Data e hora do erro", example = "2026-06-18T20:00:00Z")
                Instant timestamp,
        @Schema(description = "Codigo HTTP", example = "400") int status,
        @Schema(description = "Descricao curta do status HTTP", example = "Bad Request")
                String error,
        @Schema(description = "Mensagem legivel do erro", example = "Erro de validacao")
                String message,
        @Schema(description = "Caminho da requisicao", example = "/prazos") String path,
        @Schema(description = "Erros de validacao por campo") List<FieldError> fieldErrors) {

    @Schema(description = "Erro de validacao associado a um campo")
    public record FieldError(
            @Schema(description = "Nome do campo", example = "descricao") String field,
            @Schema(description = "Mensagem de validacao", example = "descricao e' obrigatoria")
                    String message) {}
}
