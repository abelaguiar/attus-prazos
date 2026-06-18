package com.attus.prazos.repository;

import com.attus.prazos.domain.Prazo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Porta de acesso ao banco para a entidade Prazo.
 *
 * <p>Note que e' uma INTERFACE e nao tem implementacao escrita por nos.
 * O Spring Data JPA gera a implementacao em tempo de execucao. So por
 * estender JpaRepository&lt;Prazo, Long&gt; ja ganhamos de graca:
 * save, findById, findAll, deleteById, count, etc.
 *
 * <p>O &lt;Prazo, Long&gt; diz: "este repositorio guarda Prazo, cuja chave
 * primaria e' do tipo Long".
 */
public interface PrazoRepository extends JpaRepository<Prazo, Long> {
}
