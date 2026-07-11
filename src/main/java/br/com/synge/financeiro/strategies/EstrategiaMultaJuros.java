package br.com.synge.financeiro.strategies;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EstrategiaMultaJuros {
    BigDecimal calcularAcrescimo(BigDecimal valorOriginal, LocalDate dataVencimento, LocalDate dataCalculo);
}