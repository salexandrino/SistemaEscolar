package br.com.kutuar.seguranca.services.observers;

import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogEscolaObserver implements EscolaCadastradaObserver {

    private static final Logger logger = LoggerFactory.getLogger("AUDITORIA");

    @Override
    public void aoCadastrarEscola(Escola escola, Usuario gestor, String senhaGerada) {
        logger.info("[AUDITORIA] Escola cadastrada: '{}' (id: {}) | Gestor: {}",
                escola.getNome(), escola.getId(), gestor.getEmail());
    }
}