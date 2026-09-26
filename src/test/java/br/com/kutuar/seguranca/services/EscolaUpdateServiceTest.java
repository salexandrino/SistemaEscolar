package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.AtualizarEscolaDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EscolaUpdateServiceTest {
    private final EscolaRepository repository = mock(EscolaRepository.class);
    private final EscolaService service = new EscolaService(repository, mock(UsuarioRepository.class), mock(PasswordService.class));
    private final AuthUser admin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "00000000000");

    @Test
    void atualizacaoParcialPreservaCamposAusentesEImutaveis() {
        Escola escola = escolaCompleta();
        UUID idOriginal = escola.getId();
        UUID tenantOriginal = escola.getTenantId();
        LocalDateTime criadoEmOriginal = escola.getCriadoEm();
        when(repository.findById(escola.getId())).thenReturn(Optional.of(escola));
        AtualizarEscolaDTO dto = new AtualizarEscolaDTO();
        dto.setNome("Escola Renomeada");

        service.atualizarEscola(escola.getId(), dto, admin);

        assertEquals("Escola Renomeada", escola.getNome());
        assertEquals("81999999999", escola.getTelefone());
        assertEquals("Rua Original, 10", escola.getEndereco());
        assertEquals(true, escola.isTemInternet());
        assertEquals(idOriginal, escola.getId());
        assertEquals(tenantOriginal, escola.getTenantId());
        assertEquals(criadoEmOriginal, escola.getCriadoEm());
        verify(repository).update(escola);
    }

    @Test
    void booleanNuloNaoAlteraEFalseAlteraExplicitamente() {
        Escola escola = escolaCompleta();
        when(repository.findById(escola.getId())).thenReturn(Optional.of(escola));

        service.atualizarEscola(escola.getId(), new AtualizarEscolaDTO(), admin);
        assertEquals(true, escola.isTemInternet());

        AtualizarEscolaDTO dto = new AtualizarEscolaDTO();
        dto.setTemInternet(false);
        service.atualizarEscola(escola.getId(), dto, admin);
        assertEquals(false, escola.isTemInternet());
    }

    private Escola escolaCompleta() {
        Escola escola = new Escola();
        escola.setId(UUID.randomUUID());
        escola.setTenantId(UUID.randomUUID());
        escola.setCriadoEm(LocalDateTime.now().minusDays(3));
        escola.setNome("Escola Original");
        escola.setTelefone("81999999999");
        escola.setEndereco("Rua Original, 10");
        escola.setTemInternet(true);
        escola.setStatus("ATIVA");
        return escola;
    }
}
