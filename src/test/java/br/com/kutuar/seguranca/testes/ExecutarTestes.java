package br.com.kutuar.seguranca.testes;

public class ExecutarTestes {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        SUITE DE TESTES AUTOMATIZADOS - SYNGE      ");
        System.out.println("==================================================");

        try {
            // 1. Roda os testes de Escola (Criação e Status Padrão)
            TesteEscola.rodar();
            System.out.println();

            // 2. Roda os testes de Usuário (Bloqueios, Ativação e Multitenancy)
            TesteUsuario.rodar();
            System.out.println();

            // 3. Roda os testes de Autenticação/Segurança e Padrão Strategy
            TesteAutenticacao.rodar();

            System.out.println("\n==================================================");
            System.out.println("       🎉 SUCESSO: O SISTEMA ESTÁ SEGURO!       ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.out.println("\n==================================================");
            System.out.println("       🚨 EXCEÇÃO ENCONTRADA NOS TESTES!         ");
            System.out.println("==================================================");
            e.printStackTrace();
            System.exit(1);
        }
    }
}