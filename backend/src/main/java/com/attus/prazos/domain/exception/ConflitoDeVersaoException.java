package com.attus.prazos.domain.exception;

public class ConflitoDeVersaoException extends RuntimeException {

    public ConflitoDeVersaoException(Long id) {
        super(
                "O prazo "
                        + id
                        + " foi modificado por outra operação. Recarregue e tente novamente.");
    }
}
