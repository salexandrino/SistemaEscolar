package br.com.kutuar.academico.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentoAluno {
    private UUID id;
    private UUID tenantId;
    private UUID idAluno;
    private String tipo;
    private String referencia;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
