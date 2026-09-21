package com.agenciaviagens.api.repository;

import com.agenciaviagens.api.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository de Avaliacao.
 */
@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
}
