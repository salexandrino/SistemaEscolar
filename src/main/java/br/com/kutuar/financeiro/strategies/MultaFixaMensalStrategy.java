package br.com.kutuar.financeiro.strategies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MultaFixaMensalStrategy implements EstrategiaMultaJuros {
    @Override
    public BigDecimal calcularAcrescimo(BigDecimal valorOriginal, LocalDate dataVencimento, LocalDate dataCalculo) {
        if (!dataCalculo.isAfter(dataVencimento)) return BigDecimal.ZERO;

        BigDecimal multaFixa = new BigDecimal("20.00");
        long mesesAtraso = ChronoUnit.MONTHS.between(dataVencimento, dataCalculo);
        if (mesesAtraso == 0) mesesAtraso = 1; // Mínimo de um mês caso tenha passado do vencimento

        BigDecimal jurosMensal = valorOriginal.multiply(new BigDecimal("0.02")).multiply(new BigDecimal(mesesAtraso));
        return multaFixa.add(jurosMensal);
    }
}