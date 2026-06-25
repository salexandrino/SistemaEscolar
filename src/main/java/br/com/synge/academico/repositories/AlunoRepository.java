package br.com.synge.academico.repositories;

import br.com.synge.academico.models.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, UUID> {
    List<Aluno> findByEscolaId(UUID escolaId);
    Optional<Aluno> findByIdAndEscolaId(UUID id, UUID escolaId);
    boolean existsByCpfAndEscolaId(String cpf, UUID escolaId);
}
