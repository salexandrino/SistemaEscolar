package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.BusinessException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EscolaStatusServiceTest {
    private final EscolaRepository repository = mock(EscolaRepository.class);
    private final EscolaService service = new EscolaService(repository, mock(UsuarioRepository.class), mock(PasswordService.class));
    private final AuthUser admin = new AuthUser(null, null, null, Perfil.SUPER_ADMIN, null);

    @Test void ativaEscolaInativaEAtualizaDataSemUpdateCompleto() {
        Escola escola = escola("INATIVA");
        when(repository.findById(escola.getId())).thenReturn(Optional.of(escola));
        when(repository.updateStatus(eq(escola.getId()), eq(br.com.kutuar.seguranca.enums.EscolaStatus.INATIVA),
                eq(br.com.kutuar.seguranca.enums.EscolaStatus.ATIVA), any(LocalDateTime.class))).thenReturn(true);

        Escola resultado = service.ativarEscola(escola.getId(), admin);

        assertEquals("ATIVA", resultado.getStatus());
        assertNotNull(resultado.getAtualizadoEm());
        verify(repository).updateStatus(eq(escola.getId()), eq(br.com.kutuar.seguranca.enums.EscolaStatus.INATIVA),
                eq(br.com.kutuar.seguranca.enums.EscolaStatus.ATIVA), any(LocalDateTime.class));
        verify(repository, never()).update(any());
    }

    @Test void rejeitaAtivacaoQuandoEscolaJaEstaAtivaSemAtualizar() {
        Escola escola = escola("ATIVA");
        when(repository.findById(escola.getId())).thenReturn(Optional.of(escola));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.ativarEscola(escola.getId(), admin));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
        assertEquals("A escola já está ativa.", erro.getMessage());
        verify(repository, never()).updateStatus(any(), any(), any(), any());
    }

    @Test void inativaEscolaAtivaERejeitaEscolaInativa() {
        Escola ativa = escola("ATIVA");
        when(repository.findById(ativa.getId())).thenReturn(Optional.of(ativa));
        when(repository.updateStatus(eq(ativa.getId()), any(), any(), any())).thenReturn(true);
        assertEquals("INATIVA", service.inativarEscola(ativa.getId(), admin).getStatus());

        Escola inativa = escola("INATIVA");
        when(repository.findById(inativa.getId())).thenReturn(Optional.of(inativa));
        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.inativarEscola(inativa.getId(), admin));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
        assertEquals("A escola já está inativa.", erro.getMessage());
    }

    @Test void retornaNaoEncontradaERecusaPerfilNaoSuperAdmin() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.ativarEscola(id, admin));
        assertThrows(AuthorizationException.class, () -> service.ativarEscola(id,
                new AuthUser(null, null, null, Perfil.GESTOR, null)));
    }

    private Escola escola(String status) {
        Escola escola = new Escola();
        escola.setId(UUID.randomUUID());
        escola.setNome("Escola teste");
        escola.setStatus(status);
        return escola;
    }
}
