package com.attus.prazos.infrastructure.persistence;

import com.attus.prazos.domain.Prazo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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
        entity.setDescricaoHash(hashDescricao(prazo.getDescricao()));
        entity.setDataPrazo(prazo.getDataPrazo());
        entity.setStatus(prazo.getStatus());
        entity.setCriadoEm(prazo.getCriadoEm());
        entity.setCumpridoEm(prazo.getCumpridoEm());
        entity.setVersion(prazo.getVersion());
        return entity;
    }

    /**
     * Hash da descricao para a constraint de unicidade. E' um detalhe de persistencia (existe so
     * para o indice), por isso vive aqui, e nao no dominio.
     */
    private String hashDescricao(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponivel", ex);
        }
    }
}
