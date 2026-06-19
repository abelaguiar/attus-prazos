package com.attus.prazos.service;

public class PrazoNaoEncontradoException extends RuntimeException {

    public PrazoNaoEncontradoException(Long id) {
        super("Prazo não encontrado: " + id);
    }
}
