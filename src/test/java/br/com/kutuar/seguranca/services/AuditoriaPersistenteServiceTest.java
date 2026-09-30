package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.repositories.AuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
