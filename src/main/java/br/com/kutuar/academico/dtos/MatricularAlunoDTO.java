package br.com.kutuar.academico.dtos;

import java.util.UUID;

public class MatricularAlunoDTO {
    private UUID idTurma;

    public UUID getIdTurma() { return idTurma; }
    public void setIdTurma(UUID idTurma) { this.idTurma = idTurma; }
}
