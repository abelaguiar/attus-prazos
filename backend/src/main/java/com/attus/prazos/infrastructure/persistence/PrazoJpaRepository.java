package com.attus.prazos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrazoJpaRepository extends JpaRepository<PrazoJpaEntity, Long> {
}
