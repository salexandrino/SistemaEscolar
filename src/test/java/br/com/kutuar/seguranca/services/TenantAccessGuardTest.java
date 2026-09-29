package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantAccessGuardTest {
    private final UUID escolaA = UUID.randomUUID();
    private final UUID escolaB = UUID.randomUUID();

    @AfterEach void cleanup() { AuthUserContext.clear(); }

    @Test void gestorDaEscolaASoAcessaRecursoDaEscolaA() {
        autenticar(Perfil.GESTOR);
        assertDoesNotThrow(() -> TenantAccessGuard.assertCurrentTenant(escolaA));
        assertThrows(NotFoundException.class, () -> TenantAccessGuard.assertCurrentTenant(escolaB));
    }

    @Test void secretariaEProfessorDaEscolaANaoAcessamRecursosDaEscolaB() {
        autenticar(Perfil.SECRETARIA);
        assertThrows(NotFoundException.class, () -> TenantAccessGuard.assertCurrentTenant(escolaB));
        autenticar(Perfil.PROFESSOR);
        assertThrows(NotFoundException.class, () -> TenantAccessGuard.assertCurrentTenant(escolaB));
    }

    @Test void financeiroDaEscolaANaoAcessaDadosDaEscolaB() {
        autenticar(Perfil.FINANCEIRO);
        assertThrows(NotFoundException.class, () -> TenantAccessGuard.assertCurrentTenant(escolaB));
    }

    @Test void tenantAtualVemSomenteDoUsuarioAutenticado() {
        autenticar(Perfil.GESTOR);
        assertEquals(escolaA, TenantAccessGuard.currentTenant());
    }

    private void autenticar(Perfil perfil) {
        AuthUserContext.setAuthUser(new AuthUser(UUID.randomUUID(), escolaA, escolaA, perfil, "52998224725"));
    }
}
