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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Entity
@Table(
        name = "prazo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_prazo_processo_descricao_hash_data",
                columnNames = {"numero_processo", "descricao_hash", "data_prazo"}))
public class Prazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false)
    private String numeroProcesso;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "descricao_hash", nullable = false, length = 64)
    private String descricaoHash;

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
        this.numeroProcesso = NumeroProcesso.normalizar(numeroProcesso);
        this.descricao = descricao;
        this.descricaoHash = hashDescricao(descricao);
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
        this.descricaoHash = hashDescricao(descricao);
        this.dataPrazo = dataPrazo;
    }

    /**
     * descricaoHash sustenta o indice unico sem indexar o text completo; calculamos junto da
     * descricao para mante-los sempre em sincronia.
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

    public String getDescricaoHash() {
        return descricaoHash;
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
