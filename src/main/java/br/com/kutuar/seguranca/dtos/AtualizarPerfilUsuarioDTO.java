package br.com.kutuar.seguranca.dtos;

import br.com.kutuar.seguranca.enums.Perfil;

public class AtualizarPerfilUsuarioDTO {
    private Perfil perfil;

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
