package br.com.synge.seguranca.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class RecuperacaoSenha {
    private UUID id;
    private UUID usuarioId;
    private UUID tenantId; // Novo campo
    private String codigo;
    private LocalDateTime expiraEm; // Renomeado de 'expiracao'
    private boolean usado; // Renomeado de 'utilizado'
    private LocalDateTime criadoEm;

    public RecuperacaoSenha() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

// Compatibilidade com o Repository

    public LocalDateTime getExpiracao() {
        return expiraEm;
    }

    public void setExpiracao(LocalDateTime expiracao) {
        this.expiraEm = expiracao;
    }

    public boolean isUtilizado() {
        return usado;
    }

    public void setUtilizado(boolean utilizado) {
        this.usado = utilizado;
    }


    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
