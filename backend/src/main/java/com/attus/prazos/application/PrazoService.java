package com.attus.prazos.application;

import com.attus.prazos.application.port.in.PrazoUseCase;
import com.attus.prazos.application.port.out.PrazoRepositoryPort;
import com.attus.prazos.domain.Prazo;
import com.attus.prazos.domain.exception.ConflitoDeVersaoException;
import com.attus.prazos.domain.exception.PrazoNaoEncontradoException;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrazoService implements PrazoUseCase {

    private static final Logger log = LoggerFactory.getLogger(PrazoService.class);

    private final PrazoRepositoryPort repository;

    public PrazoService(PrazoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Prazo criar(String numeroProcesso, String descricao, LocalDate dataPrazo) {
        Prazo prazo = Prazo.novo(numeroProcesso, descricao, dataPrazo);
        Prazo salvo = repository.salvar(prazo);
        log.info("Prazo criado id={} numeroProcesso={} dataPrazo={}",
                salvo.getId(), numeroProcesso, dataPrazo);
        return salvo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prazo> listar() {
        return repository.listar();
    }

    @Override
    @Transactional(readOnly = true)
    public Prazo buscarPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new PrazoNaoEncontradoException(id));
    }

    @Override
    @Transactional
    public Prazo marcarComoCumprido(Long id) {
        Prazo prazo = buscarPorId(id);
        prazo.marcarComoCumprido();
        Prazo salvo = repository.salvar(prazo);
        log.info("Prazo cumprido id={} numeroProcesso={}", salvo.getId(), salvo.getNumeroProcesso());
        return salvo;
    }

    @Override
    @Transactional
    public Prazo atualizar(Long id, String descricao, LocalDate dataPrazo, Long versaoCliente) {
        Prazo prazo = buscarPorId(id);
        if (!prazo.getVersion().equals(versaoCliente)) {
            log.warn("Conflito de versão id={} versaoCliente={} versaoAtual={}",
                    id, versaoCliente, prazo.getVersion());
            throw new ConflitoDeVersaoException(id);
        }
        prazo.atualizar(descricao, dataPrazo);
        Prazo salvo = repository.salvar(prazo);
        log.info("Prazo atualizado id={} versaoAtual={}", id, salvo.getVersion());
        return salvo;
    }
}
