package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.dtos.EventoAuditoriaResumoDTO;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.repositories.AuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuditoriaPersistenteServiceTest {

    @Test
    void persisteExecutorPerfilTenantEInformacoesSegurasDoEvento() {
        AuditoriaRepository repository = mock(AuditoriaRepository.class);
        AuditoriaPersistenteService service = new AuditoriaPersistenteService(repository);
        UUID executorId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID entidadeId = UUID.randomUUID();
        AuthUser executor = new AuthUser(executorId, null, null, Perfil.SUPER_ADMIN, "00000000000");

        service.registrar(executor, tenantId, "ESCOLA_CRIADA", "ESCOLA", entidadeId, "gestor_inicial_criado=true");

        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(repository).save(captor.capture());
        EventoAuditoria evento = captor.getValue();
        assertNotNull(evento.getId());
        assertNotNull(evento.getCriadoEm());
        assertEquals(executorId, evento.getExecutorId());
        assertEquals(Perfil.SUPER_ADMIN, evento.getExecutorPerfil());
        assertEquals(tenantId, evento.getTenantId());
        assertEquals("ESCOLA_CRIADA", evento.getAcao());
        assertEquals("ESCOLA", evento.getEntidade());
        assertEquals(entidadeId, evento.getEntidadeId());
        assertEquals("gestor_inicial_criado=true", evento.getDetalhes());
    }

    @Test
    void paginaConsultaEConverteDatasParaIntervaloInclusivoNoDiaFinal() {
        AuditoriaRepository repository = mock(AuditoriaRepository.class);
        AuditoriaPersistenteService service = new AuditoriaPersistenteService(repository);
        LocalDateTime inicio = LocalDate.of(2026, 9, 1).atStartOfDay();
        LocalDateTime fimExclusivo = LocalDate.of(2026, 10, 1).atStartOfDay();
        var item = new EventoAuditoriaResumoDTO(UUID.randomUUID(), inicio, "Admin", UUID.randomUUID(),
                "LOGIN", "USUARIO", "login válido");
        when(repository.countFiltered("LOGIN", "USUARIO", inicio, fimExclusivo)).thenReturn(21L);
        when(repository.findFiltered("LOGIN", "USUARIO", inicio, fimExclusivo, 10, 10)).thenReturn(List.of(item));

        var page = service.listar("LOGIN", "USUARIO", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), 2, 10);

        assertEquals(2, page.page());
        assertEquals(3, page.totalPages());
        assertEquals(List.of(item), page.items());
        verify(repository).findFiltered("LOGIN", "USUARIO", inicio, fimExclusivo, 10, 10);
    }

    @Test
    void limitaPaginaForaDaFaixaAultimaPaginaExistente() {
        AuditoriaRepository repository = mock(AuditoriaRepository.class);
        AuditoriaPersistenteService service = new AuditoriaPersistenteService(repository);
        when(repository.countFiltered(null, null, null, null)).thenReturn(21L);
        when(repository.findFiltered(null, null, null, null, 10, 20)).thenReturn(List.of());

        var page = service.listar(null, null, null, null, 999, 10);

        assertEquals(3, page.page());
        verify(repository).findFiltered(null, null, null, null, 10, 20);
    }

    @Test
    void rejeitaPaginacaoEIntervaloDeDatasInvalidosAntesDoBanco() {
        AuditoriaRepository repository = mock(AuditoriaRepository.class);
        AuditoriaPersistenteService service = new AuditoriaPersistenteService(repository);

        assertThrows(IllegalArgumentException.class, () -> service.listar(null, null, null, null, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listar(null, null, null, null, 1, 101));
        assertThrows(IllegalArgumentException.class, () -> service.listar(null, null,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 9, 1), 1, 20));
        verifyNoInteractions(repository);
    }
}
