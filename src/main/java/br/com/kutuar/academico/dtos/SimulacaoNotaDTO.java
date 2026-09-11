package br.com.kutuar.academico.dtos;

import java.math.BigDecimal;

public record SimulacaoNotaDTO(
        BigDecimal mediaAtual,
        BigDecimal notaHipotetica
) {}