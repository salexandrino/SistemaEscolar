package br.com.kutuar.financeiro.observers;

import br.com.kutuar.academico.models.Aluno;
import br.com.kutuar.academico.services.estados.SituacaoAluno;
import br.com.kutuar.academico.services.observers.AlunoSituacaoObserver;
import br.com.kutuar.financeiro.services.MensalidadeService;

public class CancelarMensalidadesObserver implements AlunoSituacaoObserver {
    private final MensalidadeService mensalidadeService;

    public CancelarMensalidadesObserver(MensalidadeService mensalidadeService) {
        this.mensalidadeService = mensalidadeService;
    }

    @Override
    public void aoMudarSituacao(Aluno aluno, SituacaoAluno anterior, SituacaoAluno nova, String motivo) {
        if (nova == SituacaoAluno.CANCELADO || nova == SituacaoAluno.TRANSFERIDO) {
            // TODO: MensalidadeService ainda não tem um método de cancelamento em massa
            // por aluno. Quando existir (ex: cancelarPendentes(tenantId, idAluno)),
            // descomentar a linha abaixo.
            // mensalidadeService.cancelarPendentes(aluno.getTenantId(), aluno.getId());
        }
    }
}