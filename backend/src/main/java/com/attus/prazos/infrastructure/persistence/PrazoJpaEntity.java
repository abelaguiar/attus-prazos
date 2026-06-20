package com.attus.prazos.infrastructure.persistence;

import com.attus.prazos.domain.StatusPrazo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mapeamento JPA do Prazo — detalhe de infraestrutura. O dominio (com.attus.prazos.domain.Prazo)
 * nao conhece esta classe; a conversao acontece no PrazoMapper.
 */
@Entity
@Table(
        name = "prazo",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_prazo_processo_descricao_hash_data",
                        columnNames = {"numero_processo", "descricao_hash", "data_prazo"}))
public class PrazoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false)
    private String numeroProcesso;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    /**
     * Hash SHA-256 da descricao. Sustenta o indice unico sem indexar o {@code text} completo (que
     * pode estourar o limite de entrada do B-tree no PostgreSQL). Derivado da descricao no
     * PrazoMapper.
     */
    @Column(name = "descricao_hash", nullable = false, length = 64)
    private String descricaoHash;

    @Column(name = "data_prazo", nullable = false)
    private LocalDate dataPrazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPrazo status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "cumprido_em")
    private LocalDateTime cumpridoEm;

    @Version private Long version;

    protected PrazoJpaEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricaoHash() {
        return descricaoHash;
    }

    public void setDescricaoHash(String descricaoHash) {
        this.descricaoHash = descricaoHash;
    }

    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDate dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public StatusPrazo getStatus() {
        return status;
    }

    public void setStatus(StatusPrazo status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getCumpridoEm() {
        return cumpridoEm;
    }

    public void setCumpridoEm(LocalDateTime cumpridoEm) {
        this.cumpridoEm = cumpridoEm;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
