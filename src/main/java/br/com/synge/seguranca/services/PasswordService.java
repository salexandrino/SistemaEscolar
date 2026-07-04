package br.com.synge.seguranca.services;

import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.utils.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    private static final int LOG_ROUNDS = 10;

    public String hash(String senha) {
        ValidationUtil.validatePasswordComplexity(senha); // Reutiliza validação centralizada
        return BCrypt.hashpw(senha, BCrypt.gensalt(LOG_ROUNDS));
    }

    public boolean matches(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(senha, hash);
    }

    public boolean verificar(String senha, String senhaHash) {

        logger.info("=== PASSWORD SERVICE ===");
        logger.info("Senha recebida: '{}'", senha);
        logger.info("Hash recebido: {}", senhaHash);

        boolean resultado = BCrypt.checkpw(senha, senhaHash);

        logger.info("Resultado BCrypt: {}", resultado);

        return resultado;
    }}