package br.com.kutuar.seguranca.models;

import br.com.kutuar.seguranca.enums.Perfil;

import java.time.LocalDateTime;
import java.util.UUID;

/** Registro imutável de uma ação relevante para rastreabilidade de negócio. */
public class EventoAuditoria {

    private UUID id;
    private UUID executorId;
    private Perfil executorPerfil;
    private UUID tenantId;
    private String acao;
    private String entidade;
    private UUID entidadeId;
    private String detalhes;
    private LocalDateTime criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getExecutorId() { return executorId; }
    public void setExecutorId(UUID executorId) { this.executorId = executorId; }
    public Perfil getExecutorPerfil() { return executorPerfil; }
    public void setExecutorPerfil(Perfil executorPerfil) { this.executorPerfil = executorPerfil; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    public String getEntidade() { return entidade; }
    public void setEntidade(String entidade) { this.entidade = entidade; }
    public UUID getEntidadeId() { return entidadeId; }
    public void setEntidadeId(UUID entidadeId) { this.entidadeId = entidadeId; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
