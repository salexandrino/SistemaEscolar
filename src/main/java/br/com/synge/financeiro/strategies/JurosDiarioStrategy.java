package br.com.synge.financeiro.strategies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.math.RoundingMode;

/**
 * Multa e juros de mora para mensalidade escolar, dentro dos limites da
 * Lei 9.870/1999 (art. 6º) e do CDC: multa fixa de até 2% sobre o valor
 * em atraso, mais juros de mora de até 1% ao mês (pro-rata dia).
 *
 * ATENÇÃO: a versão anterior desta classe cobrava 1% AO DIA (~365% ao ano),
 * um valor muito acima do limite legal para mensalidade escolar. Foi
 * corrigido para 2% de multa única + 1%/mês pro-rata die.
 */
public class JurosDiarioStrategy implements EstrategiaMultaJuros {

    private static final BigDecimal MULTA_PERCENTUAL = new BigDecimal("0.02");   // 2% (limite legal)
    private static final BigDecimal JUROS_MENSAL = new BigDecimal("0.01");       // 1% ao mês
    private static final BigDecimal DIAS_NO_MES = new BigDecimal("30");

    @Override
    public BigDecimal calcularAcrescimo(BigDecimal valorOriginal, LocalDate dataVencimento, LocalDate dataCalculo) {
        if (!dataCalculo.isAfter(dataVencimento)) return BigDecimal.ZERO;

        long diasAtraso = ChronoUnit.DAYS.between(dataVencimento, dataCalculo);

        BigDecimal multa = valorOriginal.multiply(MULTA_PERCENTUAL);

        BigDecimal taxaDiaria = JUROS_MENSAL.divide(DIAS_NO_MES, 10, RoundingMode.HALF_UP);
        BigDecimal juros = valorOriginal.multiply(taxaDiaria).multiply(new BigDecimal(diasAtraso));

        return multa.add(juros).setScale(2, RoundingMode.HALF_UP);
    }
}