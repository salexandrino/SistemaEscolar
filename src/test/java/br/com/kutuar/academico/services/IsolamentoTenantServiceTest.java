package br.com.kutuar.academico.services;

import br.com.kutuar.academico.models.Disciplina;
import br.com.kutuar.academico.repositories.DisciplinaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class IsolamentoTenantServiceTest {
    @Test
    void cadaTenantConsultaSomenteOsPropriosDados() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        DisciplinaRepository repository = mock(DisciplinaRepository.class);
        DisciplinaService service = new DisciplinaService(repository);
        Disciplina disciplinaA = new Disciplina();
        disciplinaA.setNome("Dados A");
        Disciplina disciplinaB = new Disciplina();
        disciplinaB.setNome("Dados B");
        when(repository.listar(tenantA)).thenReturn(List.of(disciplinaA));
        when(repository.listar(tenantB)).thenReturn(List.of(disciplinaB));

        assertEquals(List.of(disciplinaA), service.listar(tenantA));
        assertEquals(List.of(disciplinaB), service.listar(tenantB));
        verify(repository).listar(tenantA);
        verify(repository).listar(tenantB);
    }
}
