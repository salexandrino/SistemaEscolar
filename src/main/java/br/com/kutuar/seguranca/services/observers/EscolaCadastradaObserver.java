package br.com.kutuar.seguranca.services.observers;

import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;

/**
 * Padrão Observer (mesmo molde do AlunoSituacaoObserver em academico):
 * quem precisar reagir ao cadastro de uma escola (auditoria, e-mail,
 * etc.) implementa esta interface e se registra no EscolaService via
 * adicionarObserver(), sem o EscolaService precisar conhecer os detalhes
 * de cada reação.
 */
public interface EscolaCadastradaObserver {
    void aoCadastrarEscola(Escola escola, Usuario gestor, String senhaGerada);
}