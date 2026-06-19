package com.attus.prazos.application.port.in;

import com.attus.prazos.domain.Prazo;
import java.time.LocalDate;
import java.util.List;

/**
 * Porta de ENTRADA: os casos de uso que a aplicacao oferece. O controller
 * (adaptador de entrada) depende desta interface, nao da implementacao.
 */
public interface PrazoUseCase {

    Prazo criar(String numeroProcesso, String descricao, LocalDate dataPrazo);

    List<Prazo> listar();

    Prazo buscarPorId(Long id);

    Prazo marcarComoCumprido(Long id);

    Prazo atualizar(Long id, String descricao, LocalDate dataPrazo, Long versaoCliente);
}
