package br.com.synge.seguranca.utils;

import br.com.synge.seguranca.exceptions.ValidationException;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");

    public static void validateCpf(String cpf) {
        if (cpf == null || !CPF_PATTERN.matcher(cpf).matches()) {
            throw new ValidationException("CPF deve estar no formato 000.000.000-00.");
        }
        // Adicionar lógica de validação matemática do CPF aqui, se necessário
        if (!isValidCpfMath(cpf)) {
            throw new ValidationException("CPF inválido.");
        }
    }

    // Lógica de validação matemática do dígito verificador de CPF
    private static boolean isValidCpfMath(String cpf) {
        cpf = cpf.replace(".", "").replace("-", ""); // Remove formatação
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false; // CPFs com todos os dígitos iguais são inválidos
        }

        int[] numbers = new int[11];
        for (int i = 0; i < 11; i++) {
            numbers[i] = Character.getNumericValue(cpf.charAt(i));
        }

        // Valida primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += numbers[i] * (10 - i);
        }
        int result = 11 - (sum % 11);
        int d1 = (result > 9) ? 0 : result;

        if (d1 != numbers[9]) {
            return false;
        }

        // Valida segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += numbers[i] * (11 - i);
        }
        result = 11 - (sum % 11);
        int d2 = (result > 9) ? 0 : result;

        return d2 == numbers[10];
    }

    public static void validatePasswordComplexity(String password) {
        // Regras de senha: mínimo 8, máximo 64, uma maiúscula, uma minúscula, um número, um caractere especial.
        if (password == null || password.length() < 8 || password.length() > 64) {
            throw new ValidationException("A senha deve ter entre 8 e 64 caracteres.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("A senha deve conter pelo menos uma letra maiúscula.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("A senha deve conter pelo menos uma letra minúscula.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("A senha deve conter pelo menos um número.");
        }
        if (!password.matches(".*[!@#$%^&*()-_=+\\|\\[{\\]};:'\",<.>/?].*")) {
            throw new ValidationException("A senha deve conter pelo menos um caractere especial.");
        }
    }
}
