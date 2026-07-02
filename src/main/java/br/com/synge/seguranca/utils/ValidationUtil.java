package br.com.synge.seguranca.utils;

import br.com.synge.seguranca.exceptions.ValidationException;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\(\\d{2}\\)\\d{5}-\\d{4}$"); // (99)99999-9999

    public static void validateNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().length() < 5 || nomeCompleto.trim().length() > 120) {
            throw new ValidationException("Nome completo deve ter entre 5 e 120 caracteres e não pode ser vazio.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("O e-mail não pode estar em branco.");
        }
        if (email.length() > 150) {
            throw new ValidationException("O e-mail deve ter no máximo 150 caracteres.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Formato de e-mail inválido.");
        }
    }

    public static void validateCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new ValidationException("O CPF não pode estar em branco.");
        }
        // Aceitar CPF no formato mascarado 000.000.000-00 ou como 11 dígitos numéricos
        if (CPF_PATTERN.matcher(cpf).matches()) {
            return;
        }
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return;
        }
        throw new ValidationException("CPF deve estar no formato 000.000.000-00 ou conter 11 dígitos.");
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

    public static void validateTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new ValidationException("O telefone não pode estar em branco.");
        }
        if (!PHONE_PATTERN.matcher(telefone).matches()) {
            throw new ValidationException("Formato de telefone inválido. Use (99)99999-9999.");
        }
    }

    public static void validatePasswordComplexity(String password) {
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

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static String extractNumbers(String text) {
        return text != null ? text.replaceAll("\\D", "") : "";
    }
}