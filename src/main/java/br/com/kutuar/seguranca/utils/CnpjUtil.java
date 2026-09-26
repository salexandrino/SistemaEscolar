package br.com.kutuar.seguranca.utils;

/** Centraliza o formato canônico usado para persistir e comparar CNPJs. */
public final class CnpjUtil {

    private CnpjUtil() {
    }

    public static String normalizar(String cnpj) {
        return cnpj == null ? null : cnpj.replaceAll("\\D", "");
    }
}
