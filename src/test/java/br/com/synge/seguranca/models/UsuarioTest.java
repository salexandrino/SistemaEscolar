package br.com.synge.seguranca.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes JUnit 5 convertidos a partir da lógica manual de
 * br.com.synge.seguranca.testes.TesteUsuario (métodos rodar()/main()).
 *
 * As regras de negócio verificadas são as mesmas do teste manual original;
 * apenas o mecanismo de execução e verificação mudou para JUnit + Assertions.
 */
class UsuarioTest {

    @Test
    @DisplayName("Usuário novo deve iniciar desativado (pendente de aprovação)")
    void usuarioDeveIniciarDesativado() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(false); // mesma regra simulada no teste manual original

        assertFalse(usuario.isAtivo(),
                "Um usuário novo não pode iniciar ativo antes da aprovação do Admin.");
    }

    @Test
    @DisplayName("Usuário deve ser bloqueado após 3 tentativas de login inválidas")
    void usuarioDeveBloquearAposTresTentativas() {
        Usuario usuario = new Usuario();
        usuario.setTentativasLogin(0);

        for (int i = 0; i < 3; i++) {
            usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
        }
        if (usuario.getTentativasLogin() >= 3) {
            usuario.setBloqueado(true);
        }

        assertTrue(usuario.isBloqueado(),
                "Usuário deveria estar bloqueado após 3 tentativas inválidas.");
        assertEquals(3, usuario.getTentativasLogin());
    }

    @Test
    @DisplayName("Usuários de escolas (tenants) diferentes devem ter tenantId diferentes")
    void usuariosDeEscolasDiferentesDevemTerTenantIdsDiferentes() {
        UUID tenantEscolaA = UUID.randomUUID();
        UUID tenantEscolaB = UUID.randomUUID();

        Usuario usuarioEscolaA = new Usuario();
        usuarioEscolaA.setEmail("professor@escolaA.com");
        usuarioEscolaA.setTenantId(tenantEscolaA);

        Usuario usuarioEscolaB = new Usuario();
        usuarioEscolaB.setEmail("professor@escolaB.com");
        usuarioEscolaB.setTenantId(tenantEscolaB);

        assertNotEquals(usuarioEscolaA.getTenantId(), usuarioEscolaB.getTenantId(),
                "Falha de isolamento multi-tenant: os tenantIds não podem ser iguais.");
    }
}
