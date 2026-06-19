package com.attus.prazos.exception;

public class OrdenacaoInvalidaException extends RuntimeException {

    public OrdenacaoInvalidaException(String campo) {
        super("Campo de ordenação inválido: " + campo);
    }
}
