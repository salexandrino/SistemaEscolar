package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.EscolaExclusaoImpactoDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.BusinessException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EscolaExclusaoImpactoServiceTest {
    private final EscolaRepository repository = mock(EscolaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final EscolaService service = new EscolaService(repository, usuarioRepository, mock(PasswordService.class));
    private final AuthUser superAdmin = new AuthUser(null, null, null, Perfil.SUPER_ADMIN, null);

    @Test void analisaEscolaAtivaComContagensDoTenantDaEscola() {
        Escola escola = escola("ATIVA");
        escola.setTenantId(UUID.randomUUID()); // não é o tenant usado pelas tabelas do produto
        Map<String, Long> dependencias = Map.of("aluno", 2L, "usuario", 1L);
        when(repository.findById(escola.getId())).thenReturn(Optional.of(escola));
        when(repository.contarDependenciasPorTenant(escola.getId())).thenReturn(dependencias);

        EscolaExclusaoImpactoDTO impacto = service.analisarImpactoExclusao(escola.getId(), superAdmin);

        assertFalse(impacto.podeExcluir());
        assertTrue(impacto.motivosBloqueio().stream().anyMatch(m -> m.contains("inativa")));
        assertEquals(2L, impacto.dependencias().get("aluno"));
        verify(repository).contarDependenciasPorTenant(escola.getId());
        verify(repository, never()).delete(any());
        verifyNoInteractions(usuarioRepository);
    }

    @Test void analisaEscolaInativaMasMantemExclusaoBloqueadaPelaRetencao() {
        Escola escola = escola("INATIVA");
        when(repository.findById(escola.getId())).thenReturn(Optional.of(escola));
        when(repository.contarDependenciasPorTenant(escola.getId())).thenReturn(Map.of("aluno", 0L));

        EscolaExclusaoImpactoDTO impacto = service.analisarImpactoExclusao(escola.getId(), superAdmin);

        assertFalse(impacto.podeExcluir());
        assertTrue(impacto.motivosBloqueio().stream().anyMatch(m -> m.contains("política de retenção")));
    }

    @Test void recusaPerfilSemPermissaoEEscolaInexistente() {
        UUID id = UUID.randomUUID();
        assertThrows(AuthorizationException.class, () -> service.analisarImpactoExclusao(id,
                new AuthUser(null, null, null, Perfil.GESTOR, null)));

        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.analisarImpactoExclusao(id, superAdmin));
    }

    @Test void endpointsLegadosPermanecemBloqueadosSemRemoverDados() {
        UUID id = UUID.randomUUID();

        assertThrows(BusinessException.class, () -> service.excluirEscola(id, superAdmin));
        assertThrows(BusinessException.class, () -> service.deletarEscola(id, superAdmin));

        verifyNoInteractions(repository, usuarioRepository);
    }

    private Escola escola(String status) {
        Escola escola = new Escola();
        escola.setId(UUID.randomUUID());
        escola.setNome("Escola de teste");
        escola.setStatus(status);
        return escola;
    }
}
