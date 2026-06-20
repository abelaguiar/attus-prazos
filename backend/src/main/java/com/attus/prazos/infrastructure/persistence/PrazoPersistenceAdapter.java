package com.attus.prazos.infrastructure.persistence;

import com.attus.prazos.application.port.out.PrazoRepositoryPort;
import com.attus.prazos.domain.Prazo;
import com.attus.prazos.domain.exception.ConflitoDeVersaoException;
import com.attus.prazos.domain.exception.PrazoDuplicadoException;
import java.util.List;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Adaptador de SAIDA: implementa a porta usando Spring Data + JPA.
 *
 * <p>Aqui (e somente aqui) traduzimos excecoes de INFRAESTRUTURA para
 * excecoes de DOMINIO, para que as camadas de cima nao conhecam JPA/Hibernate.
 */
@Component
public class PrazoPersistenceAdapter implements PrazoRepositoryPort {

    private static final String UK_PRAZO_DUPLICADO = "uk_prazo_processo_descricao_hash_data";

    private final PrazoJpaRepository repository;
    private final PrazoMapper mapper;

    public PrazoPersistenceAdapter(PrazoJpaRepository repository, PrazoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Prazo salvar(Prazo prazo) {
        try {
            // saveAndFlush força o INSERT/UPDATE agora, para a violacao estourar aqui.
            PrazoJpaEntity salvo = repository.saveAndFlush(mapper.toEntity(prazo));
            return mapper.toDomain(salvo);
        } catch (DataIntegrityViolationException ex) {
            if (violouConstraint(ex, UK_PRAZO_DUPLICADO)) {
                throw new PrazoDuplicadoException();
            }
            throw ex;
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflitoDeVersaoException(prazo.getId());
        }
    }

    @Override
    public Optional<Prazo> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Prazo> listar() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    private boolean violouConstraint(DataIntegrityViolationException ex, String nomeConstraint) {
        Throwable causa = ex.getCause();
        while (causa != null) {
            if (causa instanceof ConstraintViolationException cve) {
                String nome = cve.getConstraintName();
                return nome != null && nome.toLowerCase().contains(nomeConstraint);
            }
            causa = causa.getCause();
        }
        return false;
    }
}
