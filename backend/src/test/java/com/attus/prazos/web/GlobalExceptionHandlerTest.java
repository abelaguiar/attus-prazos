package com.attus.prazos.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveMapearConflitoDeConcorrenciaPara409() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/prazos/1");
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException("Prazo", 1L);

        ApiError corpo = handler.handleConcorrencia(ex, request);

        assertThat(corpo.status()).isEqualTo(409);
        assertThat(corpo.error()).isEqualTo("Conflict");
        assertThat(corpo.path()).isEqualTo("/prazos/1");
    }
}
