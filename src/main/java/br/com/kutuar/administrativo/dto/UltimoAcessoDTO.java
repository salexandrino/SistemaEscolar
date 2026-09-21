package br.com.kutuar.administrativo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UltimoAcessoDTO {
    private UUID idUsuario;
    private String nomeCompleto;
    private String perfil;
    private LocalDateTime ultimoLogin;
    private String nomeEscola;

    public UUID getIdUsuario() { return idUsuario; }
    public void setIdUsuario(UUID idUsuario) { this.idUsuario = idUsuario; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }
    public String getNomeEscola() { return nomeEscola; }
    public void setNomeEscola(String nomeEscola) { this.nomeEscola = nomeEscola; }
}
