package br.com.synge.administrativo.repositories;

import br.com.synge.administrativo.models.ParametroEscola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParametroEscolaRepository extends JpaRepository<ParametroEscola, UUID> {
    Optional<ParametroEscola> findByEscolaIdAndChave(UUID escolaId, String chave);
}
