package br.com.synge.academico.services;

import br.com.synge.academico.dtos.LancamentoFrequenciaDTO;
import br.com.synge.academico.models.Frequencia;
import br.com.synge.academico.repositories.FrequenciaRepository;
import br.com.synge.academico.repositories.MatriculaRepository;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.time.LocalDateTime;
import java.util.UUID;

public class FrequenciaService {

    private final FrequenciaRepository frequenciaRepository;
    private final MatriculaRepository matriculaRepository;

    public FrequenciaService(FrequenciaRepository frequenciaRepository, MatriculaRepository matriculaRepository) {
        this.frequenciaRepository = frequenciaRepository;
        this.matriculaRepository = matriculaRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public void registrar(LancamentoFrequenciaDTO dto) {
        if (dto.getSituacao() == null || dto.getSituacao().isBlank()) {
            throw new ValidationException("Situação da frequência é obrigatória.");
        }

        UUID tenantId = tenant();

        if (!matriculaRepository.existeAtiva(tenantId, dto.getIdAluno(), dto.getIdTurma())) {
            throw new ValidationException("Aluno não está matriculado ativamente nesta turma.");
        }

        Frequencia f = new Frequencia();
        f.setId(UUID.randomUUID());
        f.setTenantId(tenantId);
        f.setIdTurma(dto.getIdTurma());
        f.setIdDisciplina(dto.getIdDisciplina());
        f.setIdAluno(dto.getIdAluno());
        f.setData(dto.getData());
        f.setSituacao(dto.getSituacao().toUpperCase());
        f.setObservacao(dto.getObservacao());
        f.setCriadoEm(LocalDateTime.now());
        f.setAtualizadoEm(LocalDateTime.now());

        frequenciaRepository.salvar(f);
    }
}
