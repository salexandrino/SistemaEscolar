package br.com.synge.academico.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class BoletimDTO {
    private UUID idAluno;
    private UUID idTurma;
    private UUID idDisciplina;
    private BigDecimal media;
    // Nullable de propósito: o sistema ainda não tem controle de frequência
    // (não existe tabela/model de chamada). Usar "0" aqui, como fazia
    // a versão anterior, mentia dizendo "aluno sem nenhuma falta" quando na
    // verdade o dado simplesmente não existe. null == "não disponível".
    private Integer totalPresencas;
    private Integer totalFaltas;
    private Integer totalFaltasJustificadas;
    private String situacao;

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }

    public UUID getIdTurma() { return idTurma; }
    public void setIdTurma(UUID idTurma) { this.idTurma = idTurma; }

    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }

    public BigDecimal getMedia() { return media; }
    public void setMedia(BigDecimal media) { this.media = media; }

    public Integer getTotalPresencas() { return totalPresencas; }
    public void setTotalPresencas(Integer totalPresencas) { this.totalPresencas = totalPresencas; }

    public Integer getTotalFaltas() { return totalFaltas; }
    public void setTotalFaltas(Integer totalFaltas) { this.totalFaltas = totalFaltas; }

    public Integer getTotalFaltasJustificadas() { return totalFaltasJustificadas; }
    public void setTotalFaltasJustificadas(Integer totalFaltasJustificadas) { this.totalFaltasJustificadas = totalFaltasJustificadas; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
}