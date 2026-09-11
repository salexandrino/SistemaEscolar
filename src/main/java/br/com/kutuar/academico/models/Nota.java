package br.com.kutuar.academico.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Nota(
        UUID id,
        UUID tenantId,
        UUID idAvaliacao,
        UUID idAluno,
        BigDecimal valor,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}