package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.EventoAuditoria;

import java.util.List;
import java.util.UUID;

public interface AuditoriaService {

    void registrar(AuthUser executor, UUID tenantId, String acao, String entidade, UUID entidadeId, String detalhes);

    default List<EventoAuditoria> listar() {
        throw new UnsupportedOperationException("Listagem de auditoria indisponível.");
    }

    static AuditoriaService semPersistencia() {
        return (executor, tenantId, acao, entidade, entidadeId, detalhes) -> { };
    }
}
