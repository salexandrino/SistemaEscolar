package br.com.kutuar.administrativo;

import br.com.kutuar.administrativo.dto.AlertaSistemaDTO;
import br.com.kutuar.administrativo.dto.AtividadeRecenteDTO;
import br.com.kutuar.administrativo.dto.DashboardDTO;
import br.com.kutuar.administrativo.dto.SuperAdminDashboardResponseDTO;
import br.com.kutuar.administrativo.dto.UltimoAcessoDTO;
import br.com.kutuar.administrativo.repositories.DashboardRepository;
import br.com.kutuar.administrativo.services.DashboardService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardTest {
    @Test
    void colecoesNuncaSaoNulas() {
        DashboardDTO dto = new DashboardDTO();
        for (boolean atribuirNull : List.of(false, true)) {
            if (atribuirNull) {
                dto.setMeses(null); dto.setCrescimentoEscolas(null); dto.setCrescimentoUsuarios(null);
                dto.setUsuariosPorPerfil(null); dto.setEscolasRecentes(null); dto.setUsuariosRecentes(null);
            }
            assertEquals(List.of(), dto.getMeses());
            assertEquals(List.of(), dto.getCrescimentoEscolas());
            assertEquals(List.of(), dto.getCrescimentoUsuarios());
            assertEquals(Map.of(), dto.getUsuariosPorPerfil());
            assertEquals(List.of(), dto.getEscolasRecentes());
            assertEquals(List.of(), dto.getUsuariosRecentes());
        }
    }

    @Test
    void compoeContagensSemReconsultarTotalEUsaOMesmoPeriodo() {
        DashboardRepository repository = mock(DashboardRepository.class);
        when(repository.countEscolas()).thenReturn(new DashboardRepository.ContagemEscolas(5, 3, 2));
        when(repository.countUsuariosPorPerfil()).thenReturn(Map.of("GESTOR", 2L, "PROFESSOR", 4L));
        when(repository.countUsuariosPendentes()).thenReturn(1L);
        DashboardDTO dto = new DashboardService(repository).buscarDashboard();
        assertEquals(5, dto.getTotalEscolas()); assertEquals(3, dto.getEscolasAtivas());
        assertEquals(2, dto.getEscolasInativas()); assertEquals(6, dto.getTotalUsuarios());
        assertEquals(1, dto.getUsuariosPendentes()); assertEquals(6, dto.getMeses().size());
        ArgumentCaptor<LocalDate> periodo = ArgumentCaptor.forClass(LocalDate.class);
        verify(repository).findCrescimentoMensal(eq("escola"), periodo.capture());
        verify(repository).findCrescimentoMensal(eq("usuario"), eq(periodo.getValue()));
        assertEquals(1, periodo.getValue().getDayOfMonth());
        verify(repository).countEscolas(); verify(repository).countUsuariosPorPerfil();
        verify(repository).countUsuariosPendentes(); verify(repository).findUltimasEscolas();
        verify(repository).findUltimosUsuarios(); verifyNoMoreInteractions(repository);
    }

    @Test
    void rejeitaTabelaArbitrariaAntesDeConectar() {
        assertThrows(IllegalArgumentException.class, () -> new DashboardRepository()
                .findCrescimentoMensal("usuario; DROP TABLE escola", LocalDate.now()));
    }

    @Test
    void alertasClassificamSeveridadePorQuantidade() {
        DashboardRepository repository = mock(DashboardRepository.class);
        when(repository.countEscolas()).thenReturn(new DashboardRepository.ContagemEscolas(20, 9, 11));
        when(repository.countUsuariosPendentes()).thenReturn(10L);

        List<AlertaSistemaDTO> alertas = new DashboardService(repository).buscarAlertasSistema();

        assertEquals("CRITICO", severidade(alertas, "ESCOLAS_INATIVAS"));
        assertEquals("ATENCAO", severidade(alertas, "USUARIOS_PENDENTES"));
    }

    @Test
    void alertasIgnoramQuantidadesZeradasOuNegativas() {
        DashboardRepository repository = mock(DashboardRepository.class);
        when(repository.countEscolas()).thenReturn(new DashboardRepository.ContagemEscolas(0, 0, 0));

        List<AlertaSistemaDTO> alertas = new DashboardService(repository).buscarAlertasSistema();

        assertTrue(alertas.isEmpty());
    }

    @Test
    void buscarAlertasSistemaNaoExecutaConsultasCarasDoDashboardCompleto() {
        DashboardRepository repository = mock(DashboardRepository.class);
        when(repository.countEscolas()).thenReturn(new DashboardRepository.ContagemEscolas(5, 5, 0));

        new DashboardService(repository).buscarAlertasSistema();

        verify(repository).countEscolas();
        verify(repository).countUsuariosPendentes();
        verify(repository).countUsuariosBloqueados();
        verify(repository).countUsuariosSemAcessoRecente(anyInt());
        verify(repository).countTentativasLoginSuspeitas();
        verify(repository, never()).findUltimasEscolas();
        verify(repository, never()).findUltimosUsuarios();
        verify(repository, never()).findCrescimentoMensal(anyString(), any(LocalDate.class));
        verify(repository, never()).countUsuariosPorPerfil();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void dashboardSuperAdminPopulaCamposNovos() {
        DashboardRepository repository = mock(DashboardRepository.class);
        UltimoAcessoDTO acesso = new UltimoAcessoDTO();
        acesso.setIdUsuario(UUID.randomUUID());
        acesso.setNomeCompleto("Mirela");
        acesso.setPerfil("SUPER_ADMIN");
        acesso.setUltimoLogin(LocalDateTime.now());

        AtividadeRecenteDTO atividade = new AtividadeRecenteDTO();
        atividade.setTipo("USUARIO_LOGOU");
        atividade.setDescricao("Login realizado: Mirela");
        atividade.setOcorridoEm(LocalDateTime.now());

        when(repository.countEscolas()).thenReturn(new DashboardRepository.ContagemEscolas(3, 2, 1));
        when(repository.countUsuariosPendentes()).thenReturn(1L);
        when(repository.countUsuariosPorPerfil()).thenReturn(Map.of("SUPER_ADMIN", 1L));
        when(repository.findCrescimentoMensal(eq("escola"), any(LocalDate.class))).thenReturn(List.of(1L, 1L, 2L, 2L, 3L, 3L));
        when(repository.findCrescimentoMensal(eq("usuario"), any(LocalDate.class))).thenReturn(List.of(1L, 1L, 1L, 1L, 1L, 1L));
        when(repository.findUltimosAcessos(anyInt())).thenReturn(List.of(acesso));
        when(repository.findAtividadesRecentes(anyInt())).thenReturn(List.of(atividade));

        SuperAdminDashboardResponseDTO dto = new DashboardService(repository).buscarDashboardSuperAdmin();

        assertEquals(List.of(acesso), dto.getUltimosAcessos());
        assertEquals(List.of(atividade), dto.getAtividades());
        assertEquals(2, dto.getAlertas().size());
        assertEquals("ESCOLAS_INATIVAS", dto.getAlertas().get(0).getTipo());
        assertEquals("USUARIOS_PENDENTES", dto.getAlertas().get(1).getTipo());
    }

    private String severidade(List<AlertaSistemaDTO> alertas, String tipo) {
        return alertas.stream()
                .filter(alerta -> tipo.equals(alerta.getTipo()))
                .findFirst()
                .orElseThrow()
                .getSeveridade();
    }
}
