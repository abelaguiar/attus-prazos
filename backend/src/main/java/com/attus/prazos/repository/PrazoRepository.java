package com.attus.prazos.repository;

import com.attus.prazos.domain.Prazo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrazoRepository extends JpaRepository<Prazo, Long> {
}
