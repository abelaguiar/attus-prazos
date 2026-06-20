package com.attus.prazos.domain.exception;

public class PrazoNaoEncontradoException extends RuntimeException {

    public PrazoNaoEncontradoException(Long id) {
        super("Prazo não encontrado: " + id);
    }
}
