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
        // Log seguro: avisa o que está fazendo, mas não expõe os dados
        logger.debug("Processando verificação de credenciais via BCrypt.");

        if (senha == null || senhaHash == null) {
            logger.warn("Tentativa de verificação com senha ou hash nulos.");
            return false;
        }

        // Executa a checagem nativa
        boolean resultado = BCrypt.checkpw(senha, senhaHash);

        // Registra apenas o resultado final
        logger.debug("Resultado da validação de senha: {}", resultado);

        return resultado;
    }}