package com.attus.prazos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.domain.StatusPrazo;
import com.attus.prazos.repository.PrazoRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrazoServiceTest {

    @Mock
    private PrazoRepository repository;

    @InjectMocks
    private PrazoService service;

    @Test
    void criarDeveNascerComStatusPendente() {
        when(repository.save(any(Prazo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prazo resultado = service.criar("0001234-56.2026.8.26.0100", "Contestacao",
                LocalDate.now().plusDays(10));

        assertThat(resultado.getStatus()).isEqualTo(StatusPrazo.PENDENTE);
        assertThat(resultado.getNumeroProcesso()).isEqualTo("0001234-56.2026.8.26.0100");
        assertThat(resultado.getCumpridoEm()).isNull();
    }

    @Test
    void marcarComoCumpridoDeveAtualizarStatusEData() {
        Prazo prazo = new Prazo("0001234-56.2026.8.26.0100", "Contestacao", LocalDate.now().plusDays(5));
        when(repository.findById(1L)).thenReturn(Optional.of(prazo));
        when(repository.save(any(Prazo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prazo resultado = service.marcarComoCumprido(1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusPrazo.CUMPRIDO);
        assertThat(resultado.getCumpridoEm()).isNotNull();
    }

    @Test
    void buscarPorIdInexistenteDeveLancarExcecao() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(PrazoNaoEncontradoException.class)
                .hasMessageContaining("999");
    }
}
