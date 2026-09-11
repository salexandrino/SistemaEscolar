package br.com.kutuar.academico.services.observers;

import br.com.kutuar.academico.models.Aluno;
import br.com.kutuar.academico.services.estados.SituacaoAluno;

public interface AlunoSituacaoObserver {
    void aoMudarSituacao(Aluno aluno, SituacaoAluno situacaoAnterior, SituacaoAluno novaSituacao, String motivo);
}