package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.*;
import br.com.kutuar.seguranca.dtos.EscolaResumoDTO;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.models.*;
import br.com.kutuar.seguranca.repositories.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EscolaPaginacaoServiceTest {
    private final EscolaRepository repository = mock(EscolaRepository.class);
    private final EscolaService service = new EscolaService(repository, mock(UsuarioRepository.class), mock(PasswordService.class));
    private final AuthUser admin = new AuthUser(null, null, null, Perfil.SUPER_ADMIN, null);

    @Test void retornaPaginaEContagemFiltrada() {
        Escola escola = new Escola();
        when(repository.countFiltered("Kutuar", EscolaStatus.ATIVA)).thenReturn(41L);
        when(repository.findAllPaginated("Kutuar", EscolaStatus.ATIVA, 20, 20)).thenReturn(List.of(escola));
        var pagina = service.listarPaginadas("  Kutuar  ", EscolaStatus.ATIVA, 2, 20, admin);
        assertEquals(List.of(EscolaResumoDTO.from(escola)), pagina.items());
        assertEquals(41, pagina.totalItems());
        assertEquals(3, pagina.totalPages());
        assertEquals(2, pagina.page());
        assertTrue(pagina.hasNext());
        assertTrue(pagina.hasPrevious());
    }

    @Test void normalizaMinimosEVazio() {
        var pagina = service.listarPaginadas("  ", null, -5, 0, admin);
        assertEquals(1, pagina.page());
        assertEquals(1, pagina.size());
        assertEquals(0, pagina.totalPages());
        assertFalse(pagina.hasPrevious());
        assertFalse(pagina.hasNext());
        verify(repository).findAllPaginated(null, null, 1, 0);
    }

    @Test void parametrosAusentesUsamPadroesDoService() {
        var pagina = service.listarPaginadas(null, null, null, null, admin);
        assertEquals(EscolaService.DEFAULT_PAGE, pagina.page());
        assertEquals(EscolaService.DEFAULT_PAGE_SIZE, pagina.size());
        verify(repository).findAllPaginated(null, null, EscolaService.DEFAULT_PAGE_SIZE, 0);
    }

    @Test void limitaTamanhoEImpedeOverflow() {
        var pagina = service.listarPaginadas(null, null, Integer.MAX_VALUE, Integer.MAX_VALUE, admin);
        assertEquals(100, pagina.size());
        assertEquals(21474837, pagina.page());
        verify(repository).findAllPaginated(null, null, 100, 2147483600);
    }

    @Test void ultimaPaginaENaoAutorizado() {
        when(repository.countFiltered(null, null)).thenReturn(40L);
        var pagina = service.listarPaginadas(null, null, 2, 20, admin);
        assertEquals(2, pagina.totalPages());
        assertFalse(pagina.hasNext());
        clearInvocations(repository);
        assertThrows(AuthorizationException.class, () -> service.listarPaginadas(null, null, 1, 20, null));
        verifyNoInteractions(repository);
    }

    @Test void statusAceitaUrlsAntigasEIgnoraInvalidos() {
        assertEquals(EscolaStatus.ATIVA, EscolaStatus.fromFilter(" ativa "));
        assertEquals(EscolaStatus.INATIVA, EscolaStatus.fromFilter("INATIVA"));
        assertNull(EscolaStatus.fromFilter("desconhecido"));
        assertNull(EscolaStatus.fromFilter(null));
    }

    @Test void buscaStatusEPaginacaoEncaminhamOsParametrosCorretos() {
        Escola primeira = new Escola();
        Escola segunda = new Escola();
        when(repository.countFiltered("Escola", EscolaStatus.INATIVA)).thenReturn(3L);
        when(repository.findAllPaginated("Escola", EscolaStatus.INATIVA, 1, 0)).thenReturn(List.of(primeira));
        when(repository.findAllPaginated("Escola", EscolaStatus.INATIVA, 1, 1)).thenReturn(List.of(segunda));

        var primeiraPagina = service.listarPaginadas(" Escola ", EscolaStatus.INATIVA, 1, 1, admin);
        var segundaPagina = service.listarPaginadas(" Escola ", EscolaStatus.INATIVA, 2, 1, admin);

        assertEquals(1, primeiraPagina.items().size());
        assertEquals(2, segundaPagina.page());
        assertTrue(segundaPagina.hasPrevious());
        assertTrue(segundaPagina.hasNext());
        verify(repository).findAllPaginated("Escola", EscolaStatus.INATIVA, 1, 0);
        verify(repository).findAllPaginated("Escola", EscolaStatus.INATIVA, 1, 1);
    }

    @Test void contadoresPodemSerObtidosDasListasDeCadaStatus() {
        when(repository.findAll()).thenReturn(List.of(new Escola(), new Escola(), new Escola()));
        when(repository.findAllAtivas()).thenReturn(List.of(new Escola(), new Escola()));
        when(repository.findAllInativas()).thenReturn(List.of(new Escola()));

        assertEquals(3, service.listarTodas(admin).size());
        assertEquals(2, service.listarAtivas(admin).size());
        assertEquals(1, service.listarInativas(admin).size());
    }
}
