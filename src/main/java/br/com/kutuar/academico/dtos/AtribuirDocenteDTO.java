package br.com.kutuar.academico.dtos;

import java.util.UUID;

public class AtribuirDocenteDTO {
    private UUID idDisciplina;
    private UUID idProfessor;

    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }
    public UUID getIdProfessor() { return idProfessor; }
    public void setIdProfessor(UUID idProfessor) { this.idProfessor = idProfessor; }
}
