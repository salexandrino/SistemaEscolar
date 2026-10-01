package br.com.kutuar.seguranca.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventoAuditoriaResumoDTO(
        UUID id,
        LocalDateTime criadoEm,
        String executor,
        UUID executorId,
        String acao,
        String entidade,
        String detalhes) {
}