package br.com.kutuar.academico.dtos;

import java.util.List;

public class GradeProfessorDTO {
    private int cargaHorariaTotal;
    private int cargaHorariaContratual;
    private List<GradeProfessorItemDTO> itens;

    public int getCargaHorariaTotal() { return cargaHorariaTotal; }
    public void setCargaHorariaTotal(int cargaHorariaTotal) { this.cargaHorariaTotal = cargaHorariaTotal; }

    public int getCargaHorariaContratual() { return cargaHorariaContratual; }
    public void setCargaHorariaContratual(int cargaHorariaContratual) { this.cargaHorariaContratual = cargaHorariaContratual; }

    public List<GradeProfessorItemDTO> getItens() { return itens; }
    public void setItens(List<GradeProfessorItemDTO> itens) { this.itens = itens; }
}
