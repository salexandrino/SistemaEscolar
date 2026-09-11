package br.com.kutuar.academico.dtos;

import java.util.UUID;

public class GradeProfessorItemDTO {
    private UUID idTurma;
    private String nomeTurma;
    private UUID idDisciplina;
    private String nomeDisciplina;
    private int cargaHorariaAnual;

    public UUID getIdTurma() { return idTurma; }
    public void setIdTurma(UUID idTurma) { this.idTurma = idTurma; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public int getCargaHorariaAnual() { return cargaHorariaAnual; }
    public void setCargaHorariaAnual(int cargaHorariaAnual) { this.cargaHorariaAnual = cargaHorariaAnual; }
}
