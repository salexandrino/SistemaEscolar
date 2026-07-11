package br.com.synge.financeiro.strategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class JurosDiarioStrategy implements EstrategiaMultaJuros {
    private static final BigDecimal TAXA_JUROS_DIARIO = new BigDecimal("0.00033"); // ~1% ao mês

    @Override
    public BigDecimal calcularMulta(BigDecimal valorOriginal, LocalDate vencimento) {
        return BigDecimal.ZERO; // Juros diários puro
    }

    @Override
    public BigDecimal calcularJuros(BigDecimal valorOriginal, LocalDate vencimento) {
        long diasAtraso = ChronoUnit.DAYS.between(vencimento, LocalDate.now());
        if (diasAtraso <= 0) return BigDecimal.ZERO;
        return valorOriginal.multiply(TAXA_JUROS_DIARIO).multiply(new BigDecimal(diasAtraso)).setScale(2, RoundingMode.HALF_UP);
    }
}