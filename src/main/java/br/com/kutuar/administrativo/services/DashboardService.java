package br.com.kutuar.administrativo.services;


import br.com.kutuar.administrativo.dto.DashboardDTO;
import br.com.kutuar.administrativo.repositories.DashboardRepository;

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
        dto.setMeses(dashboardRepository.findMesesCrescimento());
        dto.setCrescimentoEscolas(dashboardRepository.findCrescimentoMensal("escola"));
        dto.setCrescimentoUsuarios(dashboardRepository.findCrescimentoMensal("usuario"));
        dto.setUsuariosPorPerfil(dashboardRepository.countUsuariosPorPerfil());


        return dto;
    }
}
