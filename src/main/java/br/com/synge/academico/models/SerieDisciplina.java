package br.com.synge.academico.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class SerieDisciplina {
    private UUID id;
    private UUID tenantId;
    private UUID idSerie;
    private UUID idDisciplina;
    private Integer cargaHorariaAnual;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getIdSerie() { return idSerie; }
    public void setIdSerie(UUID idSerie) { this.idSerie = idSerie; }
    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }
    public Integer getCargaHorariaAnual() { return cargaHorariaAnual; }
    public void setCargaHorariaAnual(Integer cargaHorariaAnual) { this.cargaHorariaAnual = cargaHorariaAnual; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
