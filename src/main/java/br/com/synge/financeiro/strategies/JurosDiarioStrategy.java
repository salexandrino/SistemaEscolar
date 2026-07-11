package br.com.synge.financeiro.strategies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class JurosDiarioStrategy implements EstrategiaMultaJuros {
    @Override
    public BigDecimal calcularAcrescimo(BigDecimal valorOriginal, LocalDate dataVencimento, LocalDate dataCalculo) {
        if (!dataCalculo.isAfter(dataVencimento)) return BigDecimal.ZERO;

        long diasAtraso = ChronoUnit.DAYS.between(dataVencimento, dataCalculo);
        // Aplica taxa de 1% ao dia (0.01) sobre o valor original por dia de atraso
        BigDecimal taxaDiaria = new BigDecimal("0.01");
        return valorOriginal.multiply(taxaDiaria).multiply(new BigDecimal(diasAtraso));
    }
}