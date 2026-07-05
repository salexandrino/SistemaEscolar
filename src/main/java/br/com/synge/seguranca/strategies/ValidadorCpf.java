package br.com.synge.seguranca.strategies;

import br.com.synge.seguranca.exceptions.ValidationException;

public class ValidadorCpf implements ValidadorDocumento {
    @Override
    public void validar(String cpf) {
        if (cpf == null || cpf.replaceAll("\\D", "").length() != 11) {
            // Dispara a SUA Exception que o Javalin já trata
            throw new ValidationException("CPF inválido ou mal formatado.");
        }
    }
}