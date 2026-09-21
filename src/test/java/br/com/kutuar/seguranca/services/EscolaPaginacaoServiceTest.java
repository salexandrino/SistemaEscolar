package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.*;
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
        assertEquals(List.of(escola), pagina.itens());
        assertEquals(41, pagina.total());
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
}
