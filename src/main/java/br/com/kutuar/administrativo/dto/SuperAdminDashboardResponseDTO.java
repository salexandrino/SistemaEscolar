package br.com.kutuar.administrativo.dto;

import java.util.List;

public class SuperAdminDashboardResponseDTO extends DashboardDTO {
    private List<UltimoAcessoDTO> ultimosAcessos = List.of();
    private List<AlertaSistemaDTO> alertas = List.of();
    private List<AtividadeRecenteDTO> atividades = List.of();

    public List<UltimoAcessoDTO> getUltimosAcessos() { return ultimosAcessos; }
    public void setUltimosAcessos(List<UltimoAcessoDTO> ultimosAcessos) { this.ultimosAcessos = ultimosAcessos == null ? List.of() : ultimosAcessos; }
    public List<AlertaSistemaDTO> getAlertas() { return alertas; }
    public void setAlertas(List<AlertaSistemaDTO> alertas) { this.alertas = alertas == null ? List.of() : alertas; }
    public List<AtividadeRecenteDTO> getAtividades() { return atividades; }
    public void setAtividades(List<AtividadeRecenteDTO> atividades) { this.atividades = atividades == null ? List.of() : atividades; }
}
