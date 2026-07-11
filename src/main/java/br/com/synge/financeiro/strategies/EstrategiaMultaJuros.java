package br.com.synge.financeiro.strategies;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EstrategiaMultaJuros {
    BigDecimal calcularMulta(BigDecimal valorOriginal, LocalDate dataVencimento);
    BigDecimal calcularJurosDiarios(BigDecimal valorOriginal, LocalDate dataVencimento);
}