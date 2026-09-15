package br.com.kutuar.util;

import org.mindrot.jbcrypt.BCrypt;

public class GeradorBCrypt {

    public static void main(String[] args) {
        String senha = "Admin123@";

        String hash = BCrypt.hashpw(senha, BCrypt.gensalt(10));

        System.out.println("Senha: " + senha);
        System.out.println("Hash BCrypt:");
        System.out.println(hash);

        boolean confere = BCrypt.checkpw(senha, hash);
        System.out.println("Senha confere? " + confere);
    }
}