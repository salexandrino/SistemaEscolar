package br.com.synge.financeiro.observers;

import br.com.synge.academico.models.Aluno;
import br.com.synge.academico.services.estados.SituacaoAluno;
import br.com.synge.academico.services.observers.AlunoSituacaoObserver;
import br.com.synge.financeiro.services.MensalidadeService;

public class CancelarMensalidadesObserver implements AlunoSituacaoObserver {
    private final MensalidadeService mensalidadeService;

    public CancelarMensalidadesObserver(MensalidadeService mensalidadeService) {
        this.mensalidadeService = mensalidadeService;
    }

    @Override
    public void aoMudarSituacao(Aluno aluno, SituacaoAluno anterior, SituacaoAluno nova) {
        if (nova == SituacaoAluno.CANCELADO || nova == SituacaoAluno.TRANSFERIDO) {
            //mensalidadeService.cancelarMensalidades(aluno.getTenantId(), aluno.getId());
        }
    }
}