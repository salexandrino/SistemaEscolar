package br.com.kutuar.academico.services;

import br.com.kutuar.academico.services.media.CalculadoraMediaStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class RecuperacaoService {
    private static final BigDecimal MEDIA_MINIMA = new BigDecimal("6.0");
    private final CalculadoraMediaStrategy mediaStrategy;

    public RecuperacaoService(CalculadoraMediaStrategy mediaStrategy) {
        this.mediaStrategy = mediaStrategy;
    }

    /**
     * Fórmula: notaNecessaria = (mediaMinima * 2) - mediaAtual
     */
    public BigDecimal calcularNotaNecessariaRecuperacao(BigDecimal mediaAtual) {
        if (mediaAtual.compareTo(MEDIA_MINIMA) >= 0) {
            return BigDecimal.ZERO;
        }
        return MEDIA_MINIMA.multiply(new BigDecimal("2"))
            .subtract(mediaAtual)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean simularAprovacao(BigDecimal mediaAtual, BigDecimal notaRecuperacao) {
        BigDecimal mediaFinal = mediaAtual.add(notaRecuperacao)
            .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        return mediaFinal.compareTo(MEDIA_MINIMA) >= 0;
    }
}