package com.attus.prazos.web.dto;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.domain.StatusPrazo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PrazoResponse(
        Long id,
        String numeroProcesso,
        String descricao,
        LocalDate dataPrazo,
        StatusPrazo status,
        boolean vencido,
        Instant criadoEm,
        LocalDateTime cumpridoEm,
        Long version
) {

    public static PrazoResponse from(Prazo prazo) {
        return new PrazoResponse(
                prazo.getId(),
                prazo.getNumeroProcesso(),
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
