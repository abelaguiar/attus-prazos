package com.attus.prazos.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade de dominio Prazo — um POJO puro, sem nenhuma dependencia de
 * framework (JPA, Spring). O mapeamento de banco vive em
 * infrastructure/persistence/PrazoJpaEntity.
 */
public class Prazo {

    private final Long id;
    private String numeroProcesso;
    private String descricao;
    private LocalDate dataPrazo;
    private StatusPrazo status;
    private final Instant criadoEm;
    private LocalDateTime cumpridoEm;
    private final Long version;

    /** Reconstrucao a partir da persistencia (usado pelo mapper). */
    public Prazo(Long id, String numeroProcesso, String descricao, LocalDate dataPrazo,
            StatusPrazo status, Instant criadoEm, LocalDateTime cumpridoEm, Long version) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.descricao = descricao;
        this.dataPrazo = dataPrazo;
        this.status = status;
        this.criadoEm = criadoEm;
        this.cumpridoEm = cumpridoEm;
        this.version = version;
    }

    /**
     * Criacao de um novo prazo: nasce PENDENTE, com data de criacao, ainda sem id/version.
     * O numero do processo e' normalizado para a forma canonica (so digitos) — regra de dominio,
     * para que mascarado e nao-mascarado sejam o mesmo prazo.
     */
    public static Prazo novo(String numeroProcesso, String descricao, LocalDate dataPrazo) {
        return new Prazo(null, NumeroProcesso.normalizar(numeroProcesso), descricao, dataPrazo,
                StatusPrazo.PENDENTE, Instant.now(), null, null);
    }

    public void marcarComoCumprido() {
        this.status = StatusPrazo.CUMPRIDO;
        this.cumpridoEm = LocalDateTime.now();
    }

    public void atualizar(String descricao, LocalDate dataPrazo) {
        this.descricao = descricao;
        this.dataPrazo = dataPrazo;
    }

    /** Condicao DERIVADA: nao existe coluna para isso, calculamos na hora. */
    public boolean isVencido() {
        return status == StatusPrazo.PENDENTE && dataPrazo.isBefore(LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public StatusPrazo getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getCumpridoEm() {
        return cumpridoEm;
    }

    public Long getVersion() {
        return version;
    }
}
