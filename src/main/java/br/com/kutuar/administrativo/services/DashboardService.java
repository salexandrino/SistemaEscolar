package br.com.kutuar.administrativo.services;


import br.com.kutuar.administrativo.dto.DashboardDTO;
import br.com.kutuar.administrativo.repositories.DashboardRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public DashboardDTO buscarDashboard() {

        DashboardDTO dto = new DashboardDTO();

        LocalDate mesAtual = LocalDate.now().withDayOfMonth(1);
        DashboardRepository.ContagemEscolas escolas = dashboardRepository.countEscolas();
        dto.setTotalEscolas(escolas.total());
        dto.setEscolasAtivas(escolas.ativas());
        dto.setEscolasInativas(escolas.inativas());

        dto.setUsuariosPendentes(dashboardRepository.countUsuariosPendentes());

        dto.setEscolasRecentes(dashboardRepository.findUltimasEscolas());
        dto.setUsuariosRecentes(dashboardRepository.findUltimosUsuarios());
        List<String> nomesMeses = List.of("Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set", "Out", "Nov", "Dez");
        dto.setMeses(IntStream.range(0, 6)
                .mapToObj(i -> nomesMeses.get(mesAtual.minusMonths(5 - i).getMonthValue() - 1))
                .toList());
        dto.setCrescimentoEscolas(dashboardRepository.findCrescimentoMensal("escola", mesAtual));
        dto.setCrescimentoUsuarios(dashboardRepository.findCrescimentoMensal("usuario", mesAtual));
        dto.setUsuariosPorPerfil(dashboardRepository.countUsuariosPorPerfil());
        dto.setTotalUsuarios(dto.getUsuariosPorPerfil().values().stream().mapToLong(Long::longValue).sum());


        return dto;
    }
}
