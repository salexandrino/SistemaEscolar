package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;

import java.util.Objects;
import java.util.UUID;

/** Fonte unica do tenant da requisicao e da verificacao horizontal de acesso. */
public final class TenantAccessGuard {
    private TenantAccessGuard() { }

    public static UUID currentTenant() {
        AuthUser user = AuthUserContext.getAuthUser();
        if (user == null) throw new AuthenticationException("Usuario nao autenticado.");
        if (user.getTenantId() == null) {
            throw new AuthorizationException("Este recurso exige um usuario vinculado a uma escola.");
        }
        return user.getTenantId();
    }

    /** Usa 404 para nao revelar recursos que pertencem a outra escola. */
    public static void assertCurrentTenant(UUID resourceTenantId) {
        if (!Objects.equals(currentTenant(), resourceTenantId)) {
            throw new NotFoundException("Recurso nao encontrado.");
        }
    }
}
