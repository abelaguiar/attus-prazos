package com.attus.prazos.domain.exception;

public class PrazoDuplicadoException extends RuntimeException {

    public PrazoDuplicadoException() {
        super("Já existe um prazo com este processo, descrição e data.");
    }
}
