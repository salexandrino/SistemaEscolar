package br.com.synge.seguranca.strategies;

import br.com.synge.seguranca.exceptions.ValidationException;

public class ValidadorCnpj implements ValidadorDocumento {

    private static final int[] PESOS_DIGITO_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_DIGITO_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public void validar(String cnpj) {
        if (cnpj == null) {
            throw new ValidationException("CNPJ da escola inválido ou mal formatado.");
        }

        String digits = cnpj.replaceAll("\\D", "");

        if (digits.length() != 14) {
            throw new ValidationException("CNPJ da escola inválido ou mal formatado.");
        }

        // CNPJs com todos os dígitos iguais têm formato válido mas são
        // matematicamente inválidos (mesma ideia do CPF).
        if (digits.chars().distinct().count() == 1) {
            throw new ValidationException("CNPJ da escola inválido.");
        }

        if (!digitosVerificadoresValidos(digits)) {
            throw new ValidationException("CNPJ da escola inválido.");
        }
    }

    private boolean digitosVerificadoresValidos(String cnpj) {
        int[] numeros = new int[14];
        for (int i = 0; i < 14; i++) {
            numeros[i] = Character.getNumericValue(cnpj.charAt(i));
        }

        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += numeros[i] * PESOS_DIGITO_1[i];
        }
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;

        if (digito1 != numeros[12]) {
            return false;
        }

        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += numeros[i] * PESOS_DIGITO_2[i];
        }
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;

        return digito2 == numeros[13];
    }
}