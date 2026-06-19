package com.attus.prazos.exception;

public class PrazoNaoEncontradoException extends RuntimeException {

    public PrazoNaoEncontradoException(Long id) {
        super("Prazo não encontrado: " + id);
    }
}
