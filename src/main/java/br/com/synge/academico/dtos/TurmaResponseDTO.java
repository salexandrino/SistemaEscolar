package br.com.synge.academico.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class TurmaResponseDTO {
    private UUID id;
    private UUID idAnoLetivo;
    private UUID idSerie;
    private String turno;
    private String sala;
    private int capacidade;
    private String situacao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getIdAnoLetivo() { return idAnoLetivo; }
    public void setIdAnoLetivo(UUID idAnoLetivo) { this.idAnoLetivo = idAnoLetivo; }
    public UUID getIdSerie() { return idSerie; }
    public void setIdSerie(UUID idSerie) { this.idSerie = idSerie; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
    public int getCapacidade() { return capacidade; }
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
