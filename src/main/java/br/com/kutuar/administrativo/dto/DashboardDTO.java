package br.com.kutuar.administrativo.dto;

import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;

import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private long totalEscolas;
    private long escolasAtivas;
    private long escolasInativas;
    private long totalUsuarios;
    private long usuariosPendentes;
    private List<String> meses;
    private List<Long> crescimentoEscolas;
    private List<Long> crescimentoUsuarios;
    private Map<String, Long> usuariosPorPerfil;

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

    public List<String> getMeses() { return meses; }
    public void setMeses(List<String> meses) { this.meses = meses; }
    public List<Long> getCrescimentoEscolas() { return crescimentoEscolas; }
    public void setCrescimentoEscolas(List<Long> crescimentoEscolas) { this.crescimentoEscolas = crescimentoEscolas; }
    public List<Long> getCrescimentoUsuarios() { return crescimentoUsuarios; }
    public void setCrescimentoUsuarios(List<Long> crescimentoUsuarios) { this.crescimentoUsuarios = crescimentoUsuarios; }
    public Map<String, Long> getUsuariosPorPerfil() { return usuariosPorPerfil; }
    public void setUsuariosPorPerfil(Map<String, Long> usuariosPorPerfil) { this.usuariosPorPerfil = usuariosPorPerfil; }
}
