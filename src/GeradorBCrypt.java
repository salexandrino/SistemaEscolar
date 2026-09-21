package br.com.kutuar.util;

import org.mindrot.jbcrypt.BCrypt;
import java.io.Console;
import java.util.Arrays;

public class GeradorBCrypt {

    public static void main(String[] args) {
        Console console = System.console();
        char[] senha = console == null
                ? args.length == 1 ? args[0].toCharArray() : new char[0]
                : console.readPassword("Senha para gerar o hash: ");
        if (senha.length == 0) {
            throw new IllegalArgumentException("Informe a senha como argumento ou execute em um terminal interativo.");
        }

        String hash = BCrypt.hashpw(new String(senha), BCrypt.gensalt(10));
        Arrays.fill(senha, '\0');
        System.out.println(hash);
    }
}