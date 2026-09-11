package br.com.kutuar.academico.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Serie {
    private UUID id;
    private UUID tenantId;
    private UUID idAnoLetivo;
    private String nome;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // ===== Censo Escolar (Educacenso) =====
    // Lista essencial — pode crescer se precisar diferenciar mais (ex.:
    // Fundamental Anos Iniciais vs Finais já cobre a divisão oficial 1º-5º/6º-9º).
    private String etapaEnsino; // INFANTIL_CRECHE, INFANTIL_PRE_ESCOLA, FUNDAMENTAL_ANOS_INICIAIS, FUNDAMENTAL_ANOS_FINAIS, MEDIO, EJA, TECNICO

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getIdAnoLetivo() { return idAnoLetivo; }
    public void setIdAnoLetivo(UUID idAnoLetivo) { this.idAnoLetivo = idAnoLetivo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public String getEtapaEnsino() { return etapaEnsino; }
    public void setEtapaEnsino(String etapaEnsino) { this.etapaEnsino = etapaEnsino; }
}