package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.utils.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    private static final int LOG_ROUNDS = 10;

    public String hash(String senha) {
        ValidationUtil.validatePasswordComplexity(senha);
        return BCrypt.hashpw(senha, BCrypt.gensalt(LOG_ROUNDS));
    }

    public boolean matches(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(senha, hash);
    }

    public boolean verificar(String senha, String senhaHash) {
        return matches(senha, senhaHash);
    }
}
