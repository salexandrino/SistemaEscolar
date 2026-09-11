package br.com.kutuar.academico.services;

import br.com.kutuar.academico.models.AnoLetivo;
import br.com.kutuar.academico.repositories.AnoLetivoCloneRepository;
import br.com.kutuar.academico.repositories.AnoLetivoRepository;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnoLetivoServiceTest {

    @Mock
    private AnoLetivoRepository repository;

    @Mock
    private AnoLetivoCloneRepository cloneRepository;

    @InjectMocks
    private AnoLetivoService service;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        AuthUserContext.setAuthUser(new AuthUser(UUID.randomUUID(), tenantId, null, Perfil.SUPER_ADMIN, "111.222.333-44"));
    }

    @AfterEach
    void tearDown() {
        AuthUserContext.clear();
    }

    @Test
    @DisplayName("Deve lançar erro se ano de origem não existir")
    void falhaSeOrigemNaoExistir() {
        UUID idOrigem = UUID.randomUUID();
        UUID idDestino = UUID.randomUUID();

        when(repository.buscarPorId(tenantId, idOrigem)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.clonarConfiguracoes(idOrigem, idDestino));
        
        verifyNoInteractions(cloneRepository);
    }

    @Test
    @DisplayName("Deve lançar erro se ano de destino não existir")
    void falhaSeDestinoNaoExistir() {
        UUID idOrigem = UUID.randomUUID();
        UUID idDestino = UUID.randomUUID();

        when(repository.buscarPorId(tenantId, idOrigem)).thenReturn(Optional.of(new AnoLetivo()));
        when(repository.buscarPorId(tenantId, idDestino)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.clonarConfiguracoes(idOrigem, idDestino));
        
        verifyNoInteractions(cloneRepository);
    }

    @Test
    @DisplayName("Deve clonar configurações com sucesso")
    void sucessoAoClonar() throws Exception {
        UUID idOrigem = UUID.randomUUID();
        UUID idDestino = UUID.randomUUID();

        when(repository.buscarPorId(tenantId, idOrigem)).thenReturn(Optional.of(new AnoLetivo()));
        when(repository.buscarPorId(tenantId, idDestino)).thenReturn(Optional.of(new AnoLetivo()));
        doNothing().when(cloneRepository).clonarAnoLetivo(tenantId, idOrigem, idDestino);

        service.clonarConfiguracoes(idOrigem, idDestino);

        verify(cloneRepository, times(1)).clonarAnoLetivo(tenantId, idOrigem, idDestino);
    }
}
