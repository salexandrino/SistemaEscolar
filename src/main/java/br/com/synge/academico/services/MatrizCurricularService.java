package br.com.synge.academico.services;

import br.com.synge.academico.dtos.SerieDisciplinaDTO;
import br.com.synge.academico.models.Serie;
import br.com.synge.academico.models.SerieDisciplina;
import br.com.synge.academico.repositories.SerieDisciplinaRepository;
import br.com.synge.academico.repositories.SerieRepository;
import br.com.synge.academico.repositories.DisciplinaRepository;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.util.List;
import java.util.UUID;

public class MatrizCurricularService {

    private final SerieDisciplinaRepository repository;
    private final SerieRepository serieRepository;
    private final DisciplinaRepository disciplinaRepository;

    public MatrizCurricularService(SerieDisciplinaRepository repository, SerieRepository serieRepository, DisciplinaRepository disciplinaRepository) {
        this.repository = repository;
        this.serieRepository = serieRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public SerieDisciplina definir(UUID idSerie, SerieDisciplinaDTO dto) {
        if (dto == null || dto.getIdDisciplina() == null) throw new ValidationException("Disciplina é obrigatória.");
        if (dto.getCargaHorariaAnual() == null || dto.getCargaHorariaAnual() <= 0)
            throw new ValidationException("Carga horária anual deve ser > 0.");
        UUID tenantId = tenant();
        // Garantir que série e disciplina pertencem ao tenant
        Serie serie = serieRepository.buscarPorId(tenantId, idSerie).orElseThrow(() -> new NotFoundException("Série não encontrada."));
        disciplinaRepository.buscarPorId(tenantId, dto.getIdDisciplina()).orElseThrow(() -> new NotFoundException("Disciplina não encontrada."));

        return repository.salvar(tenantId, serie.getId(), dto.getIdDisciplina(), dto.getCargaHorariaAnual());
    }

    public List<SerieDisciplina> listar(UUID idSerie) {
        UUID tenantId = tenant();
        // valida série
        serieRepository.buscarPorId(tenantId, idSerie).orElseThrow(() -> new NotFoundException("Série não encontrada."));
        return repository.listar(tenantId, idSerie);
    }

    public void remover(UUID idSerie, UUID idDisciplina) {
        UUID tenantId = tenant();
        // valida série
        serieRepository.buscarPorId(tenantId, idSerie).orElseThrow(() -> new NotFoundException("Série não encontrada."));
        repository.remover(tenantId, idSerie, idDisciplina);
    }
}
