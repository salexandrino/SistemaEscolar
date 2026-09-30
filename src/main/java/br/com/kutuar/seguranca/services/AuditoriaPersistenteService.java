package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.repositories.AuditoriaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/** Implementação obrigatória em produção; não registra segredos nos detalhes. */
public class AuditoriaPersistenteService implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaPersistenteService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public void registrar(AuthUser executor, UUID tenantId, String acao, String entidade, UUID entidadeId, String detalhes) {
        EventoAuditoria evento = new EventoAuditoria();
        evento.setId(UUID.randomUUID());
        evento.setExecutorId(executor == null ? null : executor.getUserId());
        evento.setExecutorPerfil(executor == null ? null : executor.getPerfil());
        evento.setTenantId(tenantId);
        evento.setAcao(acao);
        evento.setEntidade(entidade);
        evento.setEntidadeId(entidadeId);
        evento.setDetalhes(detalhes);
        evento.setCriadoEm(LocalDateTime.now());
        auditoriaRepository.save(evento);
    }
}
