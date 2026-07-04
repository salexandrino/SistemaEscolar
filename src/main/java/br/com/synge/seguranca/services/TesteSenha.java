package br.com.synge.seguranca.services;

import org.mindrot.jbcrypt.BCrypt;

public class TesteSenha {
    public static void main(String[] args) {

        String hash = "$2a$10$aFKSFPa9iSwzo1uxS/zHWOrjaay/9htx9RLxRhNwUVU4JKJilpagy";

        String[] senhas = {
                "Synge@123",
                "SuperAdmin@123",
                "Admin@123",
                "admin123",
                "123456",
                "12345678",
                "senha123",
                "Synge123",
                "Synge@2025",
                "Synge@2026"
        };

        for (String senha : senhas) {
            System.out.println(senha + " -> " + BCrypt.checkpw(senha, hash));
        }

    }
}
