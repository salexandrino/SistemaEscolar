package br.com.synge.academico.services;

import br.com.synge.academico.dtos.CriarProfessorDTO;
import br.com.synge.academico.dtos.ProfessorResponseDTO;
import br.com.synge.academico.models.Professor;
import br.com.synge.academico.repositories.ProfessorRepository;
import br.com.synge.academico.repositories.TurmaDisciplinaProfessorRepository;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.strategies.ValidadorCpf;
import br.com.synge.seguranca.utils.AuthUserContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfessorServiceTest {

    @Mock
    private ProfessorRepository repository;
    @Mock
    private TurmaDisciplinaProfessorRepository tdpRepository;
    @Mock
    private ValidadorCpf validadorCpf;

    @InjectMocks
    private ProfessorService service;

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
    @DisplayName("Deve falhar ao criar professor sem nome")
    void falharCriarSemNome() {
        CriarProfessorDTO dto = new CriarProfessorDTO();
        dto.setCpf("12345678909");
        assertThrows(ValidationException.class, () -> service.criar(dto));
    }

    @Test
    @DisplayName("Deve criar professor com sucesso e setar carga horária default 20")
    void criarProfessorComSucesso() {
        CriarProfessorDTO dto = new CriarProfessorDTO();
        dto.setNome("João Silva");
        dto.setCpf("12345678909");

        doNothing().when(validadorCpf).validar(dto.getCpf());
        when(repository.existsByCpf(tenantId, dto.getCpf())).thenReturn(false);

        Professor p = new Professor();
        p.setId(UUID.randomUUID());
        p.setNome(dto.getNome());
        p.setCpf(dto.getCpf());
        p.setCargaHorariaContratual(20);
        
        when(repository.criar(any(Professor.class))).thenReturn(p);

        ProfessorResponseDTO resp = service.criar(dto);

        assertNotNull(resp);
        assertEquals("João Silva", resp.getNome());
        assertEquals(20, resp.getCargaHorariaContratual());
        verify(repository, times(1)).criar(any(Professor.class));
    }
}
