package br.com.synge.seguranca.services;

import org.mindrot.jbcrypt.BCrypt;

public class GerarHAsh {

    public static void main(String[] args) {

        System.out.println(
                BCrypt.hashpw("Synge@123", BCrypt.gensalt(10))
        );

    }

}