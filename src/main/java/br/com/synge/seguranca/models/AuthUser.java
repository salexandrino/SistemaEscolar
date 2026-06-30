package br.com.synge.seguranca.models;

import br.com.synge.seguranca.enums.Perfil;

import java.util.UUID;

public class AuthUser {
    private UUID userId;
    private UUID tenantId;
    private UUID escolaId; // Já existe, apenas confirmando
    private Perfil perfil;
    private String cpf;

    public AuthUser(UUID userId, UUID tenantId, Perfil perfil, String cpf) { // Construtor atualizado
        this.userId = userId;
        this.tenantId = tenantId;
        this.escolaId = escolaId;
        this.perfil = perfil;
        this.cpf = cpf;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public String getCpf() {
        return cpf;
    }

    public boolean hasPermission(String permission) {
        return this.perfil.hasPermission(permission);
    }
}