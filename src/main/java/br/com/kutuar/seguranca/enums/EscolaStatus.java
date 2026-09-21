package br.com.kutuar.seguranca.enums;

import java.util.Locale;

public enum EscolaStatus {
    ATIVA, INATIVA;

    /** Valores desconhecidos equivalem à ausência de filtro, como na listagem anterior. */
    public static EscolaStatus fromFilter(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
