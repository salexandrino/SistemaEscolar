package br.com.synge.academico.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AlunoService alunoService;

    private UUID escolaIdA;
    private UUID escolaIdB;
    private Aluno alunoA1;
    private Aluno alunoA2;
    private Aluno alunoB1;

    @BeforeEach
    void setUp() {
        escolaIdA = UUID.randomUUID();
        escolaIdB = UUID.randomUUID();

        alunoA1 = new Aluno(UUID.randomUUID(), escolaIdA, null, "Aluno A1", "111.111.111-11", LocalDate.now(), "Branca", "", "", "ATIVO", LocalDateTime.now());
        alunoA2 = new Aluno(UUID.randomUUID(), escolaIdA, null, "Aluno A2", "222.222.222-22", LocalDate.now(), "Parda", "", "", "ATIVO", LocalDateTime.now());
        alunoB1 = new Aluno(UUID.randomUUID(), escolaIdB, null, "Aluno B1", "333.333.333-33", LocalDate.now(), "Preta", "", "", "ATIVO", LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve retornar apenas alunos da Escola A")
    void deveRetornarApenasAlunosDaEscolaA() {
        when(alunoRepository.findByEscolaId(escolaIdA)).thenReturn(Arrays.asList(alunoA1, alunoA2));

        List<Aluno> alunosEscolaA = alunoService.buscarAlunosPorEscola(escolaIdA);

        assertNotNull(alunosEscolaA);
        assertEquals(2, alunosEscolaA.size());
        assertTrue(alunosEscolaA.contains(alunoA1));
        assertTrue(alunosEscolaA.contains(alunoA2));
        assertFalse(alunosEscolaA.contains(alunoB1)); // Garante que alunos de outras escolas não são retornados
        verify(alunoRepository, times(1)).findByEscolaId(escolaIdA);
        verifyNoMoreInteractions(alunoRepository);
    }

    @Test
    @DisplayName("Deve retornar apenas alunos da Escola B")
    void deveRetornarApenasAlunosDaEscolaB() {
        when(alunoRepository.findByEscolaId(escolaIdB)).thenReturn(Arrays.asList(alunoB1));

        List<Aluno> alunosEscolaB = alunoService.buscarAlunosPorEscola(escolaIdB);

        assertNotNull(alunosEscolaB);
        assertEquals(1, alunosEscolaB.size());
        assertTrue(alunosEscolaB.contains(alunoB1));
        assertFalse(alunosEscolaB.contains(alunoA1));
        verify(alunoRepository, times(1)).findByEscolaId(escolaIdB);
        verifyNoMoreInteractions(alunoRepository);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver alunos para a escola")
    void deveRetornarListaVaziaSeNaoHouverAlunosParaAEscola() {
        UUID escolaIdC = UUID.randomUUID();
        when(alunoRepository.findByEscolaId(escolaIdC)).thenReturn(Arrays.asList());

        List<Aluno> alunosEscolaC = alunoService.buscarAlunosPorEscola(escolaIdC);

        assertNotNull(alunosEscolaC);
        assertTrue(alunosEscolaC.isEmpty());
        verify(alunoRepository, times(1)).findByEscolaId(escolaIdC);
        verifyNoMoreInteractions(alunoRepository);
    }
}
