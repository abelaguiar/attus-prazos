package com.attus.prazos.exception;

public class PrazoDuplicadoException extends RuntimeException {

    public PrazoDuplicadoException() {
        super("Já existe um prazo com este processo, descrição e data.");
    }
}
