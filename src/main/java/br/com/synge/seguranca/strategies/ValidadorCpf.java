package br.com.synge.seguranca.strategies;

import br.com.synge.seguranca.exceptions.ValidationException;

public class ValidadorCpf implements ValidadorDocumento {

    @Override
    public void validar(String cpf) {
        if (cpf == null) {
            throw new ValidationException("CPF inválido ou mal formatado.");
        }

        String digits = cpf.replaceAll("\\D", "");

        if (digits.length() != 11) {
            throw new ValidationException("CPF inválido ou mal formatado.");
        }

        // CPFs com todos os dígitos iguais (111.111.111-11, 000.000.000-00, etc.)
        // têm formato válido mas são matematicamente inválidos.
        if (digits.chars().distinct().count() == 1) {
            throw new ValidationException("CPF inválido.");
        }

        if (!digitosVerificadoresValidos(digits)) {
            throw new ValidationException("CPF inválido.");
        }
    }

    private boolean digitosVerificadoresValidos(String cpf) {
        int[] numeros = new int[11];
        for (int i = 0; i < 11; i++) {
            numeros[i] = Character.getNumericValue(cpf.charAt(i));
        }

        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += numeros[i] * (10 - i);
        }
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;

        if (digito1 != numeros[9]) {
            return false;
        }

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += numeros[i] * (11 - i);
        }
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;

        return digito2 == numeros[10];
    }
}