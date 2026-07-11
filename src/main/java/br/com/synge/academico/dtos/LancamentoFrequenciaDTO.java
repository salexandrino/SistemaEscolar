package br.com.synge.academico.dtos;

import java.time.LocalDate;
import java.util.UUID;

public class LancamentoFrequenciaDTO {
    private UUID idAluno;
    private UUID idTurma;
    private UUID idDisciplina;
    private LocalDate data;
    private String situacao; // PRESENTE, FALTA, FALTA_JUSTIFICADA, ATRASO
    private String observacao;

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }

    public UUID getIdTurma() { return idTurma; }
    public void setIdTurma(UUID idTurma) { this.idTurma = idTurma; }

    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
