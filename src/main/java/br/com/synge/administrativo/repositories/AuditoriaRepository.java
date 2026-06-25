package br.com.synge.administrativo.repositories;

import br.com.synge.administrativo.models.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, UUID> {
    List<Auditoria> findByEscolaId(UUID escolaId);
    List<Auditoria> findByUsuarioId(UUID usuarioId);
}
