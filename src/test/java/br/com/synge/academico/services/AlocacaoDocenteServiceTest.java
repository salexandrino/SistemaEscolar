package br.com.synge.academico.services;

import br.com.synge.academico.dtos.AtribuirDocenteDTO;
import br.com.synge.academico.models.Turma;
import br.com.synge.academico.repositories.ProfessorRepository;
import br.com.synge.academico.repositories.SerieDisciplinaRepository;
import br.com.synge.academico.repositories.TurmaDisciplinaProfessorRepository;
import br.com.synge.academico.repositories.TurmaRepository;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlocacaoDocenteServiceTest {

    @Mock private TurmaDisciplinaProfessorRepository tdpRepository;
    @Mock private SerieDisciplinaRepository serieDisciplinaRepository;
    @Mock private TurmaRepository turmaRepository;
    @Mock private ProfessorRepository professorRepository;

    @InjectMocks
    private AlocacaoDocenteService service;

    private UUID tenantId;
    private UUID idTurma;
    private UUID idDisciplina;
    private UUID idProfessor;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        AuthUserContext.setAuthUser(new AuthUser(UUID.randomUUID(), tenantId, null, Perfil.SUPER_ADMIN, "000.000.000-00"));
        idTurma = UUID.randomUUID();
        idDisciplina = UUID.randomUUID();
        idProfessor = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        AuthUserContext.clear();
    }

    private Turma mockTurma(UUID idSerie) {
        Turma t = new Turma();
        t.setId(idTurma);
        t.setTenantId(tenantId);
        t.setIdSerie(idSerie);
        return t;
    }

    @Test
    @DisplayName("Deve permitir atribuição quando 20h/semana (800h/ano) e disciplina 160h")
    void permitirAtribuicaoDentroDoLimite() {
        UUID idSerie = UUID.randomUUID();
        when(turmaRepository.buscarPorId(tenantId, idTurma)).thenReturn(Optional.of(mockTurma(idSerie)));
        when(serieDisciplinaRepository.existeNaMatriz(idSerie, idDisciplina)).thenReturn(true);
        when(professorRepository.existsById(tenantId, idProfessor)).thenReturn(true);
        when(tdpRepository.somatorioCargaHorariaProfessor(tenantId, idProfessor)).thenReturn(0);
        when(serieDisciplinaRepository.obterCargaHorariaAnual(idSerie, idDisciplina)).thenReturn(Optional.of(160));
        when(professorRepository.getCargaHorariaContratual(tenantId, idProfessor)).thenReturn(Optional.of(20)); // semanal

        AtribuirDocenteDTO dto = new AtribuirDocenteDTO();
        dto.setIdDisciplina(idDisciplina);
        dto.setIdProfessor(idProfessor);

        assertDoesNotThrow(() -> service.atribuir(idTurma, dto));
        verify(tdpRepository, times(1)).atribuir(tenantId, idTurma, idDisciplina, idProfessor);
    }

    @Test
    @DisplayName("Deve bloquear quando atribuição ultrapassa carga anual convertida (20h/sem=800h)")
    void bloquearQuandoUltrapassaLimiteAnual() {
        UUID idSerie = UUID.randomUUID();
        when(turmaRepository.buscarPorId(tenantId, idTurma)).thenReturn(Optional.of(mockTurma(idSerie)));
        when(serieDisciplinaRepository.existeNaMatriz(idSerie, idDisciplina)).thenReturn(true);
        when(professorRepository.existsById(tenantId, idProfessor)).thenReturn(true);
        when(tdpRepository.somatorioCargaHorariaProfessor(tenantId, idProfessor)).thenReturn(700); // já tem 700h
        when(serieDisciplinaRepository.obterCargaHorariaAnual(idSerie, idDisciplina)).thenReturn(Optional.of(160));
        when(professorRepository.getCargaHorariaContratual(tenantId, idProfessor)).thenReturn(Optional.of(20)); // semanal (800h/ano)

        AtribuirDocenteDTO dto = new AtribuirDocenteDTO();
        dto.setIdDisciplina(idDisciplina);
        dto.setIdProfessor(idProfessor);

        assertThrows(ValidationException.class, () -> service.atribuir(idTurma, dto));
        verify(tdpRepository, never()).atribuir(any(), any(), any(), any());
    }
}
