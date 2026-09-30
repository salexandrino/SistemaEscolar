package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.models.AuthUser;

import java.util.UUID;

public interface AuditoriaService {

    void registrar(AuthUser executor, UUID tenantId, String acao, String entidade, UUID entidadeId, String detalhes);

    static AuditoriaService semPersistencia() {
        return (executor, tenantId, acao, entidade, entidadeId, detalhes) -> { };
    }
}
