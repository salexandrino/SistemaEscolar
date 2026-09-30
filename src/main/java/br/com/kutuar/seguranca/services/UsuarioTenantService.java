package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.Usuario;

public class UsuarioTenantService {

    public void validarConsistencia(Usuario usuario) {
        if (usuario == null || usuario.getPerfil() == null) {
            throw new ValidationException("Perfil do usuario e obrigatorio.");
        }

        if (usuario.getPerfil() == Perfil.SUPER_ADMIN) {
            if (usuario.getTenantId() != null || usuario.getEscolaId() != null) {
                throw new ValidationException("SUPER_ADMIN deve ser um usuario global, sem tenant ou escola vinculados.");
            }
            return;
        }

        if (usuario.getTenantId() == null) {
            throw new ValidationException("Usuarios de escola devem possuir tenant.");
        }
    }

    public void validarTrocaDePerfil(Usuario usuario, Perfil novoPerfil) {
        if (novoPerfil == null) {
            throw new ValidationException("Perfil e obrigatorio.");
        }
        if (usuario == null) {
            throw new ValidationException("Usuario e obrigatorio.");
        }

        if (novoPerfil == Perfil.SUPER_ADMIN) {
            usuario.setPerfil(Perfil.SUPER_ADMIN);
            usuario.setTenantId(null);
            usuario.setEscolaId(null);
        } else {
            if (usuario.getTenantId() == null) {
                throw new ValidationException("Nao e possivel atribuir perfil escolar a usuario sem tenant.");
            }
            usuario.setPerfil(novoPerfil);
        }

        validarConsistencia(usuario);
    }
}
