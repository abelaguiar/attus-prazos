package com.attus.prazos.service;

import com.attus.prazos.domain.Prazo;
import com.attus.prazos.repository.PrazoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrazoService {

    private final PrazoRepository repository;

    public PrazoService(PrazoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Prazo criar(String numeroProcesso, String descricao, LocalDate dataPrazo) {
        Prazo prazo = new Prazo(numeroProcesso, descricao, dataPrazo);
        return repository.save(prazo);
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
        return repository.save(prazo);
    }
}
