package br.com.synge.seguranca.strategies;

import br.com.synge.seguranca.exceptions.ValidationException;

public class ValidadorCnpj implements ValidadorDocumento {
    @Override
    public void validar(String cnpj) {
        if (cnpj == null || cnpj.replaceAll("\\D", "").length() != 14) {
            // Dispara a SUA Exception que o Javalin já trata
            throw new ValidationException("CNPJ da escola inválido ou mal formatado.");
        }
    }
}