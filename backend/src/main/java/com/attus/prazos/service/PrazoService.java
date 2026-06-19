package com.attus.prazos.service;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.exception.PrazoNaoEncontradoException;
import com.attus.prazos.repository.PrazoRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrazoService {

    private static final Logger log = LoggerFactory.getLogger(PrazoService.class);

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
    public List<Prazo> listar() {
        return repository.findAll();
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
}
