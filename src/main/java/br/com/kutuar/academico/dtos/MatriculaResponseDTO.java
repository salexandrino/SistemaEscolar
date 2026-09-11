package br.com.kutuar.academico.dtos;

import java.time.LocalDate;
import java.util.UUID;

public class MatriculaResponseDTO {
    private UUID id;
    private UUID idAluno;
    private UUID idTurma;
    private LocalDate dataMatricula;
    private String status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }

    public UUID getIdTurma() { return idTurma; }
    public void setIdTurma(UUID idTurma) { this.idTurma = idTurma; }

    public LocalDate getDataMatricula() { return dataMatricula; }
    public void setDataMatricula(LocalDate dataMatricula) { this.dataMatricula = dataMatricula; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
