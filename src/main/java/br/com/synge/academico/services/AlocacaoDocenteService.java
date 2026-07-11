package br.com.synge.academico.services;

import br.com.synge.academico.dtos.AtribuirDocenteDTO;
import br.com.synge.academico.repositories.SerieDisciplinaRepository;
import br.com.synge.academico.repositories.TurmaDisciplinaProfessorRepository;
import br.com.synge.academico.repositories.TurmaRepository;
import br.com.synge.academico.repositories.ProfessorRepository;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.util.UUID;

public class AlocacaoDocenteService {

    private final TurmaDisciplinaProfessorRepository tdpRepository;
    private final SerieDisciplinaRepository serieDisciplinaRepository;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    /**
     * Número padrão de semanas letivas por ano para converter carga semanal em carga anual.
     */
    private static final int SEMANAS_LETIVAS_ANO = 40;

    public AlocacaoDocenteService(TurmaDisciplinaProfessorRepository tdpRepository,
                                  SerieDisciplinaRepository serieDisciplinaRepository,
                                  TurmaRepository turmaRepository,
                                  ProfessorRepository professorRepository) {
        this.tdpRepository = tdpRepository;
        this.serieDisciplinaRepository = serieDisciplinaRepository;
        this.turmaRepository = turmaRepository;
        this.professorRepository = professorRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public void atribuir(UUID idTurma, AtribuirDocenteDTO dto) {
        if (dto == null || dto.getIdDisciplina() == null || dto.getIdProfessor() == null) {
            throw new ValidationException("Disciplina e Professor são obrigatórios.");
        }

        UUID tenantId = tenant();

        var turma = turmaRepository.buscarPorId(tenantId, idTurma)
                .orElseThrow(() -> new NotFoundException("Turma não encontrada."));

        // validar que disciplina pertence à matriz da série da turma
        boolean disciplinaNaMatriz = serieDisciplinaRepository.existeNaMatriz(turma.getIdSerie(), dto.getIdDisciplina());
        if (!disciplinaNaMatriz) {
            throw new ValidationException("Disciplina não pertence à matriz da série da turma.");
        }

        // validar professor existente
        if (!professorRepository.existsById(tenantId, dto.getIdProfessor())) {
            throw new NotFoundException("Professor não encontrado.");
        }

        // validar carga horária contratual (tudo em unidade ANUAL)
        int atual = tdpRepository.somatorioCargaHorariaProfessor(tenantId, dto.getIdProfessor());
        int cargaDisciplina = serieDisciplinaRepository.obterCargaHorariaAnual(turma.getIdSerie(), dto.getIdDisciplina())
                .orElseThrow(() -> new ValidationException("Carga horária não definida para a disciplina na série."));
        int contratualSemanal = professorRepository.getCargaHorariaContratual(tenantId, dto.getIdProfessor())
                .orElseThrow(() -> new ValidationException("Carga horária contratual não definida para o professor."));
        int contratualAnual = contratualSemanal * SEMANAS_LETIVAS_ANO;

        if (atual + cargaDisciplina > contratualAnual) {
            throw new ValidationException("Atribuição excede a carga horária contratual do professor.");
        }

        tdpRepository.atribuir(tenantId, idTurma, dto.getIdDisciplina(), dto.getIdProfessor());
    }

    public void remover(UUID idTurma, UUID idDisciplina, UUID idProfessor) {
        UUID tenantId = tenant();
        // garantir existência da turma no tenant
        turmaRepository.buscarPorId(tenantId, idTurma).orElseThrow(() -> new NotFoundException("Turma não encontrada."));
        tdpRepository.remover(tenantId, idTurma, idDisciplina, idProfessor);
    }
}
