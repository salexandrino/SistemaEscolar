package br.com.kutuar.academico.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoricoSituacaoAluno {
    private UUID id;
    private UUID tenantId;
    private UUID idAluno;
    private String situacaoAnterior;
    private String situacaoNova;
    private String motivo;
    private LocalDateTime criadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }

    public String getSituacaoAnterior() { return situacaoAnterior; }
    public void setSituacaoAnterior(String situacaoAnterior) { this.situacaoAnterior = situacaoAnterior; }

    public String getSituacaoNova() { return situacaoNova; }
    public void setSituacaoNova(String situacaoNova) { this.situacaoNova = situacaoNova; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
