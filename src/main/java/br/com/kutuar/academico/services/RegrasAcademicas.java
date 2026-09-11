package br.com.kutuar.academico.services;

import java.math.BigDecimal;

/**
 * Regras acadêmicas centralizadas para evitar divergência entre serviços
 * (ex.: BoletimService e LancamentoNotasService usando limiares diferentes
 * para decidir aprovação/recuperação do mesmo aluno).
 */
public final class RegrasAcademicas {

    private RegrasAcademicas() {
    }

    /** Média mínima para aprovação direta, sem necessidade de recuperação. */
    public static final BigDecimal MEDIA_MINIMA_APROVACAO = new BigDecimal("6.0");
}