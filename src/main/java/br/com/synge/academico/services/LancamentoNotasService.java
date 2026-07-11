package br.com.synge.academico.services;

import br.com.synge.academico.dtos.LancamentoNotaDTO;
import br.com.synge.academico.models.Avaliacao;
import br.com.synge.academico.models.Nota;
import br.com.synge.academico.repositories.AvaliacaoRepository;
import br.com.synge.academico.repositories.MatriculaRepository;
import br.com.synge.academico.repositories.NotaRepository;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LancamentoNotasService {

    private final NotaRepository notaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final MatriculaRepository matriculaRepository;

    public LancamentoNotasService(NotaRepository notaRepository, AvaliacaoRepository avaliacaoRepository, MatriculaRepository matriculaRepository) {
        this.notaRepository = notaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    private UUID tenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Tenant inválido ou não autenticado.");
        return u.getTenantId();
    }

    public void lancar(LancamentoNotaDTO dto) {
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) < 0 || dto.getValor().compareTo(BigDecimal.TEN) > 0) {
            throw new ValidationException("A nota deve estar entre 0 e 10.");
        }

        UUID tenantId = tenant();

        Avaliacao av = avaliacaoRepository.buscarPorId(tenantId, dto.getIdAvaliacao())
                .orElseThrow(() -> new NotFoundException("Avaliação não encontrada."));

        if (!matriculaRepository.existeAtiva(tenantId, dto.getIdAluno(), av.getIdTurma())) {
            throw new ValidationException("Aluno não está matriculado ativamente nesta turma.");
        }

        Nota n = new Nota();
        n.setId(UUID.randomUUID());
        n.setTenantId(tenantId);
        n.setIdAvaliacao(dto.getIdAvaliacao());
        n.setIdAluno(dto.getIdAluno());
        n.setValor(dto.getValor());
        n.setCriadoEm(LocalDateTime.now());
        n.setAtualizadoEm(LocalDateTime.now());

        notaRepository.salvar(n);
    }
}
