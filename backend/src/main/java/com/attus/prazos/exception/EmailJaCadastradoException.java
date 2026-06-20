package com.attus.prazos.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Já existe um usuário com este e-mail.");
    }
}
