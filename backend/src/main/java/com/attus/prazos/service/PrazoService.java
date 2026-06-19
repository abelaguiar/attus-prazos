package com.attus.prazos.service;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.exception.ConflitoDeVersaoException;
import com.attus.prazos.exception.OrdenacaoInvalidaException;
import com.attus.prazos.exception.PrazoNaoEncontradoException;
import com.attus.prazos.repository.PrazoRepository;
import java.time.LocalDate;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrazoService {

    private static final Logger log = LoggerFactory.getLogger(PrazoService.class);

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of(
            "id", "numeroProcesso", "descricao", "dataPrazo", "status", "criadoEm", "cumpridoEm", "version");

    private final PrazoRepository repository;

    public PrazoService(PrazoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Prazo criar(String numeroProcesso, String descricao, LocalDate dataPrazo) {
        Prazo prazo = new Prazo(numeroProcesso, descricao, dataPrazo);
        Prazo novoPrazo = repository.save(prazo);
        log.info("Prazo criado id={} numeroProcesso={} dataPrazo={}",
                novoPrazo.getId(), numeroProcesso, dataPrazo);
        return novoPrazo;
    }

    @Transactional(readOnly = true)
    public Page<Prazo> listar(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!CAMPOS_ORDENAVEIS.contains(order.getProperty())) {
                throw new OrdenacaoInvalidaException(order.getProperty());
            }
        });
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Prazo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PrazoNaoEncontradoException(id));
    }

    @Transactional
    public Prazo marcarComoCumprido(Long id) {
        Prazo prazo = buscarPorId(id);
        prazo.marcarComoCumprido();
        Prazo atualPrazo = repository.save(prazo);
        log.info("Prazo cumprido id={} numeroProcesso={}", atualPrazo.getId(), atualPrazo.getNumeroProcesso());
        return atualPrazo;
    }

    @Transactional
    public Prazo atualizar(Long id, String descricao, LocalDate dataPrazo, Long versaoCliente) {
        Prazo prazo = buscarPorId(id);
        if (!prazo.getVersion().equals(versaoCliente)) {
            log.warn("Conflito de versão id={} versaoCliente={} versaoAtual={}",
                    id, versaoCliente, prazo.getVersion());
            throw new ConflitoDeVersaoException(id);
        }
        prazo.atualizar(descricao, dataPrazo);
        Prazo atualPrazo = repository.save(prazo);
        log.info("Prazo atualizado id={} versaoAtual={}", id, atualPrazo.getVersion());
        return atualPrazo;
    }
}
