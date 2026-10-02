package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.dtos.EventoAuditoriaResumoDTO;
import br.com.kutuar.seguranca.dtos.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AuditoriaService {

    void registrar(AuthUser executor, UUID tenantId, String acao, String entidade, UUID entidadeId, String detalhes);

    default List<EventoAuditoria> listar() {
        throw new UnsupportedOperationException("Listagem de auditoria indisponível.");
    }

    default PageResponse<EventoAuditoriaResumoDTO> listar(String acao, String entidade,
                                                           LocalDate dataInicio, LocalDate dataFim,
                                                           Integer page, Integer size) {
        throw new UnsupportedOperationException("Listagem paginada de auditoria indisponível.");
    }

    static AuditoriaService semPersistencia() {
        return (executor, tenantId, acao, entidade, entidadeId, detalhes) -> { };
    }
}
