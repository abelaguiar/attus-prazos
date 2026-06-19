package com.attus.prazos.domain;

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

@Entity
@Table(
        name = "prazo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_prazo_processo_descricao_data",
                columnNames = {"numero_processo", "descricao", "data_prazo"}))
public class Prazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false)
    private String numeroProcesso;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "data_prazo", nullable = false)
    private LocalDate dataPrazo;

    /** EnumType.STRING grava "PENDENTE"/"CUMPRIDO" como texto (legivel e seguro a mudancas de ordem). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPrazo status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "cumprido_em")
    private LocalDateTime cumpridoEm;

    @Version
    private Long version;

    protected Prazo() {
    }

    /** Construtor de criacao: um prazo nasce sempre PENDENTE e com data de criacao. */
    public Prazo(String numeroProcesso, String descricao, LocalDate dataPrazo) {
        this.numeroProcesso = numeroProcesso;
        this.descricao = descricao;
        this.dataPrazo = dataPrazo;
        this.status = StatusPrazo.PENDENTE;
        this.criadoEm = Instant.now();
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
