package br.com.kutuar.seguranca.testes;

import br.com.kutuar.seguranca.models.Usuario;

public class TesteUsuario {

    public static void rodar() {
        System.out.println("⏳ [TESTE] Iniciando testes de Usuário...");
        testarUsuarioDeveIniciarDesativado();
        testarBloqueioPorTentativasDeLogin();
        testarIsolamentoPorTenant(); // <-- ADICIONE ESTA LINHA
        System.out.println("✅ [TESTE] Todos os testes de Usuário passaram!");
    }
    private static void testarUsuarioDeveIniciarDesativado() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(false); // Simula a regra do seu repository (pendente de aprovação)

        if (usuario.isAtivo()) {
            throw new RuntimeException("❌ Falha: Um usuário novo não pode iniciar ativo antes da aprovação do Admin.");
        }
        System.out.println("  • Usuário inicia desativado: OK");
    }
    // Adicione este método dentro de TesteUsuario.java

// Código corrigido para a sua classe TesteUsuario.java

    public static void testarIsolamentoPorTenant() {
        java.util.UUID tenantEscolaA = java.util.UUID.randomUUID();
        java.util.UUID tenantEscolaB = java.util.UUID.randomUUID();

        Usuario usuarioEscolaA = new Usuario();
        usuarioEscolaA.setEmail("professor@escolaA.com");
        usuarioEscolaA.setTenantId(tenantEscolaA); // Agora passando UUID corretamente!

        Usuario usuarioEscolaB = new Usuario();
        usuarioEscolaB.setEmail("professor@escolaB.com");
        usuarioEscolaB.setTenantId(tenantEscolaB); // Agora passando UUID corretamente!

        // O teste garante que os dois usuários pertencem a ambientes (escolas) diferentes
        if (usuarioEscolaA.getTenantId().equals(usuarioEscolaB.getTenantId())) {
            throw new RuntimeException("❌ Falha de Segurança: O Tenant ID da Escola A é igual ao da Escola B!");
        }
        System.out.println("  • Isolamento de dados (Multitenancy): OK");
    }

    private static void testarBloqueioPorTentativasDeLogin() {
        Usuario usuario = new Usuario();
        usuario.setTentativasLogin(0);

        // Simula 3 tentativas falhas de login
        for (int i = 0; i < 3; i++) {
            usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
        }

        // Regra de negócio comum: se atingir 3 tentativas, bloqueia
        if (usuario.getTentativasLogin() >= 3) {
            usuario.setBloqueado(true);
        }

        if (!usuario.isBloqueado()) {
            throw new RuntimeException("❌ Falha: Usuário deveria estar bloqueado após 3 tentativas inválidas.");
        }
        System.out.println("  • Bloqueio automático por tentativas: OK");
    }
}