package br.com.synge.academico.services.observers;

import br.com.synge.academico.models.Aluno;
import br.com.synge.academico.models.HistoricoSituacaoAluno;
import br.com.synge.academico.repositories.HistoricoSituacaoAlunoRepository;
import br.com.synge.academico.services.estados.SituacaoAluno;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoricoSituacaoObserver implements AlunoSituacaoObserver {
    private final HistoricoSituacaoAlunoRepository historicoRepository;

    public HistoricoSituacaoObserver(HistoricoSituacaoAlunoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    @Override
    public void aoMudarSituacao(Aluno aluno, SituacaoAluno anterior, SituacaoAluno nova, String motivo) {
        HistoricoSituacaoAluno h = new HistoricoSituacaoAluno();
        h.setId(UUID.randomUUID());
        h.setTenantId(aluno.getTenantId());
        h.setIdAluno(aluno.getId());
        h.setSituacaoAnterior(anterior.name());
        h.setSituacaoNova(nova.name());
        h.setMotivo(motivo != null ? motivo : "Alteração de situação");
        h.setCriadoEm(LocalDateTime.now());
        historicoRepository.criar(h);
    }


}