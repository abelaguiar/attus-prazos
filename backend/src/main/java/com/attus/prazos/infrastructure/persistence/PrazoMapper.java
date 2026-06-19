package com.attus.prazos.infrastructure.persistence;

import com.attus.prazos.domain.Prazo;
import org.springframework.stereotype.Component;

@Component
public class PrazoMapper {

    public Prazo toDomain(PrazoJpaEntity entity) {
        return new Prazo(
                entity.getId(),
                entity.getNumeroProcesso(),
                entity.getDescricao(),
                entity.getDataPrazo(),
                entity.getStatus(),
                entity.getCriadoEm(),
                entity.getCumpridoEm(),
                entity.getVersion());
    }

    public PrazoJpaEntity toEntity(Prazo prazo) {
        PrazoJpaEntity entity = new PrazoJpaEntity();
        entity.setId(prazo.getId());
        entity.setNumeroProcesso(prazo.getNumeroProcesso());
        entity.setDescricao(prazo.getDescricao());
        entity.setDataPrazo(prazo.getDataPrazo());
        entity.setStatus(prazo.getStatus());
        entity.setCriadoEm(prazo.getCriadoEm());
        entity.setCumpridoEm(prazo.getCumpridoEm());
        entity.setVersion(prazo.getVersion());
        return entity;
    }
}
