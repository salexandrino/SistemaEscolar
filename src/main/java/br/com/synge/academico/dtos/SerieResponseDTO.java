package br.com.synge.academico.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class SerieResponseDTO {
    private UUID id;
    private UUID idAnoLetivo;
    private String nome;
    private String etapaEnsino;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getIdAnoLetivo() { return idAnoLetivo; }
    public void setIdAnoLetivo(UUID idAnoLetivo) { this.idAnoLetivo = idAnoLetivo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEtapaEnsino() { return etapaEnsino; }
    public void setEtapaEnsino(String etapaEnsino) { this.etapaEnsino = etapaEnsino; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}