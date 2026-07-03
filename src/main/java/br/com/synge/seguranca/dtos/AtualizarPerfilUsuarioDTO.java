package br.com.synge.seguranca.dtos;

import br.com.synge.seguranca.enums.Perfil;

public class AtualizarPerfilUsuarioDTO {
    private Perfil perfil;

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
