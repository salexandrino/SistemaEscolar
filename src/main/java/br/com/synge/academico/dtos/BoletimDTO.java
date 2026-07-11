package br.com.synge.academico.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class BoletimDTO {
    private UUID idAluno;
    private UUID idTurma;
    private UUID idDisciplina;
    private BigDecimal media;
    private int totalPresencas;
    private int totalFaltas;
    private int totalFaltasJustificadas;
    private String situacao;

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }

    public UUID getIdTurma() { return idTurma; }
    public void setIdTurma(UUID idTurma) { this.idTurma = idTurma; }

    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }

    public BigDecimal getMedia() { return media; }
    public void setMedia(BigDecimal media) { this.media = media; }

    public int getTotalPresencas() { return totalPresencas; }
    public void setTotalPresencas(int totalPresencas) { this.totalPresencas = totalPresencas; }

    public int getTotalFaltas() { return totalFaltas; }
    public void setTotalFaltas(int totalFaltas) { this.totalFaltas = totalFaltas; }

    public int getTotalFaltasJustificadas() { return totalFaltasJustificadas; }
    public void setTotalFaltasJustificadas(int totalFaltasJustificadas) { this.totalFaltasJustificadas = totalFaltasJustificadas; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
}
