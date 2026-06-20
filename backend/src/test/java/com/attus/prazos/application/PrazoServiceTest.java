package com.attus.prazos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.attus.prazos.application.port.out.PrazoRepositoryPort;
import com.attus.prazos.domain.Prazo;
import com.attus.prazos.domain.StatusPrazo;
import com.attus.prazos.domain.exception.PrazoNaoEncontradoException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrazoServiceTest {

    @Mock private PrazoRepositoryPort repository;

    @InjectMocks private PrazoService service;

    @Test
    void criarDeveNascerComStatusPendente() {
        when(repository.salvar(any(Prazo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Prazo resultado =
                service.criar(
                        "0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(10));

        assertThat(resultado.getStatus()).isEqualTo(StatusPrazo.PENDENTE);
        assertThat(resultado.getNumeroProcesso()).isEqualTo("00012345620268260100");
        assertThat(resultado.getCumpridoEm()).isNull();
    }

    @Test
    void marcarComoCumpridoDeveAtualizarStatusEData() {
        Prazo prazo =
                Prazo.novo("0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(5));
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(prazo));
        when(repository.salvar(any(Prazo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Prazo resultado = service.marcarComoCumprido(1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusPrazo.CUMPRIDO);
        assertThat(resultado.getCumpridoEm()).isNotNull();
    }

    @Test
    void buscarPorIdInexistenteDeveLancarExcecao() {
        when(repository.buscarPorId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(PrazoNaoEncontradoException.class)
                .hasMessageContaining("999");
    }
}
