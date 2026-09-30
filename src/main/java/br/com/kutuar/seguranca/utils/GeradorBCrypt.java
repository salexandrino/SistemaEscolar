package br.com.kutuar.seguranca.utils;

import org.mindrot.jbcrypt.BCrypt;

public class GeradorBCrypt {

    public static void main(String[] args) {
        String senha = "Admin123@";

        String hash = BCrypt.hashpw(
                senha,
                BCrypt.gensalt(10)
        );

        System.out.println(hash);

        System.out.println(
                BCrypt.checkpw(senha, hash)
        );
    }
}