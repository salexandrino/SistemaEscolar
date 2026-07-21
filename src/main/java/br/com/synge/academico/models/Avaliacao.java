package br.com.synge.academico.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Avaliacao(
        UUID id,
        UUID tenantId,
        UUID idTurma,
        UUID idDisciplina,
        String nome,
        BigDecimal peso,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}