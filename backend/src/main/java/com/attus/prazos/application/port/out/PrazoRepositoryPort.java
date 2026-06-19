package com.attus.prazos.application.port.out;

import com.attus.prazos.domain.Prazo;
import java.util.List;
import java.util.Optional;

/**
 * Porta de SAIDA: o que a aplicacao precisa da persistencia, falando a
 * lingua do dominio (Prazo). A infraestrutura implementa esta interface.
 */
public interface PrazoRepositoryPort {

    Prazo salvar(Prazo prazo);

    Optional<Prazo> buscarPorId(Long id);

    List<Prazo> listar();
}
