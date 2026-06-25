package br.com.synge.academico.services;

import br.com.synge.academico.models.Aluno;
import br.com.synge.academico.repositories.AlunoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AlunoService {

    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);
    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> buscarAlunosPorEscola(UUID escolaId) {
        logger.info("Buscando alunos para a escola com ID: {}", escolaId);
        return alunoRepository.findByEscolaId(escolaId);
    }

    // Outros métodos de serviço...
}
