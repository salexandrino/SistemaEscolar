package br.com.synge.seguranca.services;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    // O custo do hash (log_rounds) determina o tempo que leva para gerar um hash.
    // Um valor de 10 é um bom ponto de partida, mas pode ser ajustado.
    private static final int LOG_ROUNDS = 10;

    public String gerarHash(String senha) {
        // Validação de senha conforme regras de negócio
        if (!validarComplexidadeSenha(senha)) {
            logger.warn("Tentativa de gerar hash para senha que não atende aos requisitos de complexidade.");
            throw new IllegalArgumentException("A senha não atende aos requisitos de complexidade.");
        }
        return BCrypt.hashpw(senha, BCrypt.gensalt(LOG_ROUNDS));
    }

    public boolean verificar(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(senha, hash);
    }

    // Regras de senha: mínimo 8, máximo 64, uma maiúscula, uma minúscula, um número, um caractere especial.
    private boolean validarComplexidadeSenha(String senha) {
        if (senha == null || senha.length() < 8 || senha.length() > 64) {
            return false;
        }
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        String specialChars = "!@#$%^&*()-_=+\\|[{]};:'\",<.>/?";

        for (char ch : senha.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowerCase = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (specialChars.contains(String.valueOf(ch))) {
                hasSpecialChar = true;
            }
        }
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
}
