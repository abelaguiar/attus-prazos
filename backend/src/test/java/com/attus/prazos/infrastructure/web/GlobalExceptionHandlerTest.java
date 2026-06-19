package com.attus.prazos.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.attus.prazos.domain.exception.ConflitoDeVersaoException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveMapearConflitoPara409() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/prazos/1");
        ConflitoDeVersaoException ex = new ConflitoDeVersaoException(1L);

        ApiError corpo = handler.handleConflito(ex, request);

        assertThat(corpo.status()).isEqualTo(409);
        assertThat(corpo.error()).isEqualTo("Conflict");
        assertThat(corpo.path()).isEqualTo("/prazos/1");
    }
}
