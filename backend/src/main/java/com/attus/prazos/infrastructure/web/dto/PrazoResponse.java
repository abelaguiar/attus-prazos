package com.attus.prazos.infrastructure.web.dto;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.domain.NumeroProcesso;
import com.attus.prazos.domain.StatusPrazo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Representacao de um prazo processual")
public record PrazoResponse(
        @Schema(description = "Identificador do prazo", example = "1")
        Long id,
        @Schema(description = "Numero do processo", example = "0001234-56.2026.8.26.0100")
        String numeroProcesso,
        @Schema(description = "Descricao objetiva do ato ou compromisso", example = "Contestacao")
        String descricao,
        @Schema(description = "Data limite do prazo", example = "2026-12-31")
        LocalDate dataPrazo,
        @Schema(description = "Status do prazo", example = "PENDENTE")
        StatusPrazo status,
        @Schema(description = "Indica se o prazo pendente ja passou da data limite", example = "false")
        boolean vencido,
        @Schema(description = "Data e hora de criacao", example = "2026-06-18T20:00:00Z")
        Instant criadoEm,
        @Schema(description = "Data e hora de cumprimento, quando houver", example = "2026-06-18T21:30:00")
        LocalDateTime cumpridoEm,
        @Schema(description = "Versao usada para controle de concorrencia", example = "0")
        Long version
) {

    public static PrazoResponse from(Prazo prazo) {
        return new PrazoResponse(
                prazo.getId(),
                NumeroProcesso.formatar(prazo.getNumeroProcesso()),
                prazo.getDescricao(),
                prazo.getDataPrazo(),
                prazo.getStatus(),
                prazo.isVencido(),
                prazo.getCriadoEm(),
                prazo.getCumpridoEm(),
                prazo.getVersion()
        );
    }
}
