package br.com.synge.academico.services.observers;

import br.com.synge.academico.models.Aluno;
import br.com.synge.academico.services.estados.SituacaoAluno;

public interface AlunoSituacaoObserver {
    void aoMudarSituacao(Aluno aluno, SituacaoAluno situacaoAnterior, SituacaoAluno novaSituacao);
}