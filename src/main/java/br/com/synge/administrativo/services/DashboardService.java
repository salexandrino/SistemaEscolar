package br.com.synge.administrativo.services;


import br.com.synge.administrativo.dto.DashboardDTO;
import br.com.synge.administrativo.repositories.DashboardRepository;

public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public DashboardDTO buscarDashboard() {

        DashboardDTO dto = new DashboardDTO();

        dto.setTotalEscolas(dashboardRepository.countEscolas());

        dto.setEscolasAtivas(dashboardRepository.countEscolasAtivas());

        dto.setEscolasInativas(dashboardRepository.countEscolasInativas());

        dto.setTotalUsuarios(dashboardRepository.countUsuarios());

        dto.setUsuariosPendentes(dashboardRepository.countUsuariosPendentes());

        dto.setEscolasRecentes(dashboardRepository.findUltimasEscolas());
        dto.setUsuariosRecentes(dashboardRepository.findUltimosUsuarios());


        return dto;
    }
}