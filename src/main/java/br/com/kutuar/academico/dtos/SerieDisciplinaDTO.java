package br.com.kutuar.academico.dtos;

import java.util.UUID;

public class SerieDisciplinaDTO {
    private UUID idDisciplina;
    private Integer cargaHorariaAnual;

    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }
    public Integer getCargaHorariaAnual() { return cargaHorariaAnual; }
    public void setCargaHorariaAnual(Integer cargaHorariaAnual) { this.cargaHorariaAnual = cargaHorariaAnual; }
}
