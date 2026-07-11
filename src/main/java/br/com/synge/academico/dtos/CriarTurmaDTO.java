package br.com.synge.academico.dtos;

import java.util.UUID;

public class CriarTurmaDTO {
    private UUID idAnoLetivo;
    private UUID idSerie;
    private String turno;
    private String sala;
    private Integer capacidade;

    public UUID getIdAnoLetivo() { return idAnoLetivo; }
    public void setIdAnoLetivo(UUID idAnoLetivo) { this.idAnoLetivo = idAnoLetivo; }
    public UUID getIdSerie() { return idSerie; }
    public void setIdSerie(UUID idSerie) { this.idSerie = idSerie; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
}
