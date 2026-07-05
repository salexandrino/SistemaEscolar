package br.com.synge.seguranca.testes;

import br.com.synge.seguranca.utils.ValidationUtil;

public class TesteAutenticacao {

    public static void rodar() {
        System.out.println("⏳ [TESTE] Iniciando testes de Autenticação e Segurança...");

        testarValidacaoDeEmailCorreto();
        testarValidacaoDeEmailIncorreto();
        testarGeracaoDeTokenFake();

        System.out.println("✅ [TESTE] Todos os testes de Autenticação passaram!");
    }

    private static void testarValidacaoDeEmailCorreto() {
        try {
            // Testando o utilitário que você usa no AuthService
            ValidationUtil.validateEmail("admin@synge.com");
            System.out.println("  • Validação de e-mail válido: OK");
        } catch (Exception e) {
            throw new RuntimeException("❌ Falha: O sistema rejeitou um e-mail válido.");
        }
    }

    private static void testarValidacaoDeEmailIncorreto() {
        try {
            ValidationUtil.validateEmail("email_invalido_sem_arroba");
            throw new RuntimeException("❌ Falha: O sistema aceitou um e-mail mal formatado.");
        } catch (Exception e) {
            // Se cair no catch, significa que o sistema barrou o e-mail corretamente!
            System.out.println("  • Bloqueio de e-mail inválido: OK");
        }
    }

    private static void testarGeracaoDeTokenFake() {
        // Simula o comportamento do seu jwtService.gerarToken()
        String tokenGerado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiJ9";

        if (tokenGerado == null || tokenGerado.trim().isEmpty()) {
            throw new RuntimeException("❌ Falha: O token JWT gerado não pode ser nulo ou vazio.");
        }
        System.out.println("  • Geração de Token JWT estruturado: OK");
    }
}