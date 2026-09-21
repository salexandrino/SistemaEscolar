package br.com.kutuar.administrativo;

import br.com.kutuar.administrativo.dto.DashboardDTO;
import br.com.kutuar.administrativo.repositories.DashboardRepository;
import br.com.kutuar.administrativo.services.DashboardService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
}
