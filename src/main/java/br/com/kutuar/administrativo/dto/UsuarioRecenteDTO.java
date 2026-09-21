package br.com.kutuar.administrativo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UsuarioRecenteDTO {
    private UUID id;
    private String nomeCompleto;
    private String perfil;
    private boolean ativo;
    private boolean bloqueado;
    private LocalDateTime criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
    public String getStatus() {
        if (bloqueado) return "BLOQUEADO";
        return ativo ? "APROVADO" : "PENDENTE";
    }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}