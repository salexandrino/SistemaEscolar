package br.com.kutuar.administrativo.services;


import br.com.kutuar.administrativo.dto.AlertaSistemaDTO;
import br.com.kutuar.administrativo.dto.AtividadeRecenteDTO;
import br.com.kutuar.administrativo.dto.DashboardDTO;
import br.com.kutuar.administrativo.dto.SuperAdminDashboardResponseDTO;
import br.com.kutuar.administrativo.dto.UltimoAcessoDTO;
import br.com.kutuar.administrativo.repositories.DashboardRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DashboardService {

    private static final int LIMITE_ULTIMOS_ACESSOS = 10;
    private static final int LIMITE_ATIVIDADES = 15;
    private static final int DIAS_SEM_ACESSO_RECENTE = 30;

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

    public SuperAdminDashboardResponseDTO buscarDashboardSuperAdmin() {
        DashboardDTO base = buscarDashboard();
        SuperAdminDashboardResponseDTO dto = new SuperAdminDashboardResponseDTO();
        copiarBase(base, dto);
        dto.setUltimosAcessos(buscarUltimosAcessos());
        dto.setAlertas(buscarAlertasSistema(base));
        dto.setAtividades(buscarAtividadesRecentes());
        return dto;
    }

    public List<UltimoAcessoDTO> buscarUltimosAcessos() {
        return dashboardRepository.findUltimosAcessos(LIMITE_ULTIMOS_ACESSOS);
    }

    public List<AtividadeRecenteDTO> buscarAtividadesRecentes() {
        return dashboardRepository.findAtividadesRecentes(LIMITE_ATIVIDADES);
    }

    public List<AlertaSistemaDTO> buscarAlertasSistema() {
        DashboardRepository.ContagemEscolas escolas = dashboardRepository.countEscolas();
        long usuariosPendentes = dashboardRepository.countUsuariosPendentes();
        return comporAlertas(escolas.inativas(), usuariosPendentes,
                dashboardRepository.countUsuariosBloqueados(),
                dashboardRepository.countUsuariosSemAcessoRecente(DIAS_SEM_ACESSO_RECENTE),
                dashboardRepository.countTentativasLoginSuspeitas());
    }

    private List<AlertaSistemaDTO> buscarAlertasSistema(DashboardDTO base) {
        return comporAlertas(base.getEscolasInativas(), base.getUsuariosPendentes(),
                dashboardRepository.countUsuariosBloqueados(),
                dashboardRepository.countUsuariosSemAcessoRecente(DIAS_SEM_ACESSO_RECENTE),
                dashboardRepository.countTentativasLoginSuspeitas());
    }

    private List<AlertaSistemaDTO> comporAlertas(long escolasInativas, long usuariosPendentes,
                                                  long usuariosBloqueados, long semAcessoRecente,
                                                  long tentativasSuspeitas) {
        List<AlertaSistemaDTO> alertas = new ArrayList<>();
        adicionarAlerta(alertas, "ESCOLAS_INATIVAS", escolasInativas,
                "Existem " + escolasInativas + " escola(s) inativa(s) no sistema.",
                "/dashboard/escolas?status=inativa");
        adicionarAlerta(alertas, "USUARIOS_PENDENTES", usuariosPendentes,
                "Existem " + usuariosPendentes + " usuário(s) pendente(s) de aprovação.",
                "/dashboard/usuarios?status=pendente");
        adicionarAlerta(alertas, "USUARIOS_BLOQUEADOS", usuariosBloqueados,
                "Existem " + usuariosBloqueados + " usuário(s) bloqueado(s).",
                "/dashboard/usuarios?status=bloqueado");
        adicionarAlerta(alertas, "USUARIOS_SEM_ACESSO_RECENTE", semAcessoRecente,
                "Existem " + semAcessoRecente + " usuário(s) ativo(s) sem acesso registrado ou há mais de " + DIAS_SEM_ACESSO_RECENTE + " dias.",
                "/dashboard/usuarios?acesso=sem-acesso-recente");
        adicionarAlerta(alertas, "TENTATIVAS_LOGIN_SUSPEITAS", tentativasSuspeitas,
                "Existem " + tentativasSuspeitas + " usuário(s) com 3 ou mais tentativas de login no estado atual.",
                "/dashboard/usuarios?seguranca=tentativas-login");
        return alertas;
    }

    private void adicionarAlerta(List<AlertaSistemaDTO> alertas, String tipo, long quantidade, String mensagem, String link) {
        if (quantidade <= 0) return;
        AlertaSistemaDTO alerta = new AlertaSistemaDTO();
        alerta.setTipo(tipo);
        alerta.setSeveridade(quantidade > 10 ? "CRITICO" : "ATENCAO");
        alerta.setMensagem(mensagem);
        alerta.setQuantidade(quantidade);
        alerta.setLink(link);
        alertas.add(alerta);
    }

    private void copiarBase(DashboardDTO origem, SuperAdminDashboardResponseDTO destino) {
        destino.setTotalEscolas(origem.getTotalEscolas());
        destino.setEscolasAtivas(origem.getEscolasAtivas());
        destino.setEscolasInativas(origem.getEscolasInativas());
        destino.setTotalUsuarios(origem.getTotalUsuarios());
        destino.setUsuariosPendentes(origem.getUsuariosPendentes());
        destino.setMeses(origem.getMeses());
        destino.setCrescimentoEscolas(origem.getCrescimentoEscolas());
        destino.setCrescimentoUsuarios(origem.getCrescimentoUsuarios());
        destino.setUsuariosPorPerfil(origem.getUsuariosPorPerfil());
        destino.setEscolasRecentes(origem.getEscolasRecentes());
        destino.setUsuariosRecentes(origem.getUsuariosRecentes());
    }
}
