package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.EventoAuditoriaResumoDTO;
import br.com.kutuar.seguranca.dtos.PageResponse;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.repositories.AuditoriaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public List<EventoAuditoria> listar() {
        return auditoriaRepository.findAll();
    }

    @Override
    public PageResponse<EventoAuditoriaResumoDTO> listar(String acao, String entidade,
                                                         LocalDate dataInicio, LocalDate dataFim,
                                                         Integer page, Integer size) {
        int pagina = page == null ? 1 : page;
        int tamanho = size == null ? 20 : size;
        if (pagina < 1) throw new IllegalArgumentException("A página deve ser maior ou igual a 1.");
        if (tamanho < 1 || tamanho > 100) throw new IllegalArgumentException("O tamanho deve estar entre 1 e 100.");
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        long maxPage = Integer.MAX_VALUE / (long) tamanho + 1;
        pagina = (int) Math.min(pagina, maxPage);
        LocalDateTime inicio = dataInicio == null ? null : dataInicio.atStartOfDay();
        LocalDateTime fimExclusivo = dataFim == null ? null : dataFim.plusDays(1).atStartOfDay();
        long total = auditoriaRepository.countFiltered(acao, entidade, inicio, fimExclusivo);
        long totalPages = total / tamanho + (total % tamanho == 0 ? 0 : 1);
        pagina = (int) Math.min(pagina, Math.max(1, totalPages));
        int offset = (int) ((pagina - 1L) * tamanho);
        List<EventoAuditoriaResumoDTO> eventos = auditoriaRepository.findFiltered(
                acao, entidade, inicio, fimExclusivo, tamanho, offset);
        return new PageResponse<>(pagina, tamanho, total, eventos);
    }
}
