package br.com.synge.administrativo.dto;

import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.models.Usuario;

import java.util.List;

public class DashboardDTO {

    private long totalEscolas;
    private long escolasAtivas;
    private long escolasInativas;
    private long totalUsuarios;
    private long usuariosPendentes;

    private List<Escola> escolasRecentes;
    private List<Usuario> usuariosRecentes;

    public long getTotalEscolas() {
        return totalEscolas;
    }

    public void setTotalEscolas(long totalEscolas) {
        this.totalEscolas = totalEscolas;
    }

    public long getEscolasAtivas() {
        return escolasAtivas;
    }

    public void setEscolasAtivas(long escolasAtivas) {
        this.escolasAtivas = escolasAtivas;
    }

    public long getEscolasInativas() {
        return escolasInativas;
    }

    public void setEscolasInativas(long escolasInativas) {
        this.escolasInativas = escolasInativas;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getUsuariosPendentes() {
        return usuariosPendentes;
    }

    public void setUsuariosPendentes(long usuariosPendentes) {
        this.usuariosPendentes = usuariosPendentes;
    }

    public List<Escola> getEscolasRecentes() {
        return escolasRecentes;
    }

    public void setEscolasRecentes(List<Escola> escolasRecentes) {
        this.escolasRecentes = escolasRecentes;
    }

    public List<Usuario> getUsuariosRecentes() {
        return usuariosRecentes;
    }

    public void setUsuariosRecentes(List<Usuario> usuariosRecentes) {
        this.usuariosRecentes = usuariosRecentes;
    }
}