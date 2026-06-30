package br.com.synge.seguranca.models;

import br.com.synge.seguranca.enums.Perfil;

import java.time.LocalDateTime;
import java.util.UUID;

public class Usuario {
    private UUID id;
    private UUID tenantId;
    private UUID escolaId; // Alias para tenantId

    private String nomeCompleto;
    private String email;
    private String cpf;
    private String telefone;
    private String senhaHash;

    private Perfil perfil;
    private boolean ativo;
    private boolean bloqueado; // Novo campo
    private int tentativasLogin;
    private LocalDateTime ultimoLogin;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    private String resetPasswordToken;
    private LocalDateTime resetPasswordExpiresAt;

    public Usuario() {
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
        this.escolaId = tenantId;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(UUID escolaId) {
        this.escolaId = escolaId;
        this.tenantId = escolaId;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public int getTentativasLogin() {
        return tentativasLogin;
    }

    public void setTentativasLogin(int tentativasLogin) {
        this.tentativasLogin = tentativasLogin;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordExpiresAt() {
        return resetPasswordExpiresAt;
    }

    public void setResetPasswordExpiresAt(LocalDateTime resetPasswordExpiresAt) {
        this.resetPasswordExpiresAt = resetPasswordExpiresAt;
    }

    public String getCpfMascarado() {
        if (cpf == null || cpf.length() < 11) {
            return cpf;
        }
        return "***." + cpf.substring(4, 7) + "." + cpf.substring(8, 11) + "-**";
    }
}