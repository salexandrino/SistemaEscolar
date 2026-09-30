package br.com.kutuar.seguranca.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.io.Console;
import java.util.Scanner;

public class GeradorBCrypt {

    public static void main(String[] args) {
        String senha;
        if (args.length > 0) {
            senha = args[0];
        } else {
            Console console = System.console();
            if (console != null) {
                char[] entrada = console.readPassword("Senha para gerar o hash: ");
                senha = new String(entrada);
                java.util.Arrays.fill(entrada, '\0');
            } else {
                System.out.print("Senha para gerar o hash: ");
                senha = new Scanner(System.in).nextLine();
            }
        }

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
