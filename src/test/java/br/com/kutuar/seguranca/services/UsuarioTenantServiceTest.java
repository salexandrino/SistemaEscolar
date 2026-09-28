package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.Usuario;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioTenantServiceTest {

    private final UsuarioTenantService service = new UsuarioTenantService();

    @Test
    void permiteSuperAdminSemTenant() {
        assertDoesNotThrow(() -> service.validarConsistencia(usuarioComPerfil(Perfil.SUPER_ADMIN)));
    }

    @Test
    void rejeitaGestorSemTenant() {
        assertThrows(ValidationException.class, () -> service.validarConsistencia(usuarioComPerfil(Perfil.GESTOR)));
    }

    @Test
    void rejeitaSecretariaSemTenant() {
        assertThrows(ValidationException.class, () -> service.validarConsistencia(usuarioComPerfil(Perfil.SECRETARIA)));
    }

    @Test
    void rejeitaProfessorSemTenant() {
        assertThrows(ValidationException.class, () -> service.validarConsistencia(usuarioComPerfil(Perfil.PROFESSOR)));
    }

    @Test
    void rejeitaFinanceiroSemTenant() {
        assertThrows(ValidationException.class, () -> service.validarConsistencia(usuarioComPerfil(Perfil.FINANCEIRO)));
    }

    @Test
    void promoveUsuarioEscolarParaSuperAdminGlobal() {
        Usuario usuario = usuarioComPerfil(Perfil.GESTOR);
        usuario.setTenantId(UUID.randomUUID());
        usuario.setEscolaId(UUID.randomUUID());

        service.validarTrocaDePerfil(usuario, Perfil.SUPER_ADMIN);

        assertEquals(Perfil.SUPER_ADMIN, usuario.getPerfil());
        assertNull(usuario.getTenantId());
        assertNull(usuario.getEscolaId());
    }

    @Test
    void rejeitaPerfilEscolarParaSuperAdminSemTenant() {
        Usuario usuario = usuarioComPerfil(Perfil.SUPER_ADMIN);

        assertThrows(ValidationException.class,
                () -> service.validarTrocaDePerfil(usuario, Perfil.GESTOR));
    }

    private Usuario usuarioComPerfil(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setPerfil(perfil);
        return usuario;
    }
}
