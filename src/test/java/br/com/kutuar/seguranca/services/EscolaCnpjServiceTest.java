package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.AtualizarEscolaDTO;
import br.com.kutuar.seguranca.dtos.CriarEscolaDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EscolaCnpjServiceTest {
    private final EscolaRepository escolaRepository = mock(EscolaRepository.class);
    private final EscolaService service = new EscolaService(escolaRepository, mock(UsuarioRepository.class), mock(PasswordService.class));
    private final UUID escolaId = UUID.randomUUID();
    private final AuthUser superAdmin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "00000000000");

    @Test
    void edicaoMantemOCnpjDaPropriaEscolaENormaliza() {
        Escola escola = new Escola();
        escola.setId(escolaId);
        escola.setCnpj("04252011000110");
        when(escolaRepository.findById(escolaId)).thenReturn(Optional.of(escola));
        when(escolaRepository.existsByCnpjAndIdNot("04252011000110", escolaId)).thenReturn(false);

        AtualizarEscolaDTO dto = new AtualizarEscolaDTO();
        dto.setCnpj("04.252.011/0001-10");
        service.atualizarEscola(escolaId, dto, superAdmin);

        assertEquals("04252011000110", escola.getCnpj());
        verify(escolaRepository).update(escola);
    }

    @Test
    void edicaoParaCnpjDeOutraEscolaRetornaConflito() {
        when(escolaRepository.findById(escolaId)).thenReturn(Optional.of(new Escola()));
        when(escolaRepository.existsByCnpjAndIdNot("04252011000110", escolaId)).thenReturn(true);
        AtualizarEscolaDTO dto = new AtualizarEscolaDTO();
        dto.setCnpj("04.252.011/0001-10");

        assertThrows(ConflictException.class, () -> service.atualizarEscola(escolaId, dto, superAdmin));
        verify(escolaRepository, never()).update(any());
    }

    @Test
    void cnpjInvalidoRetornaErroDeValidacao() {
        AtualizarEscolaDTO dto = new AtualizarEscolaDTO();
        dto.setCnpj("12.345.678/0001-91");

        assertThrows(ValidationException.class, () -> service.atualizarEscola(escolaId, dto, superAdmin));
        verifyNoInteractions(escolaRepository);
    }

    @Test
    void gestorRecebeOTenantDaEscolaEReferenciaAEscola() {
        UUID tenantId = UUID.randomUUID();
        doAnswer(invocation -> {
            Escola escola = invocation.getArgument(0);
            escola.setId(escolaId);
            escola.setTenantId(tenantId);
            return escola;
        }).when(escolaRepository).save(any(Escola.class));
        when(escolaRepository.existsByCnpj("04252011000110")).thenReturn(false);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(usuarios.existsByEmail(any())).thenReturn(false);
        PasswordService senha = mock(PasswordService.class);
        when(senha.hash(any())).thenReturn("hash");
        EscolaService cadastro = new EscolaService(escolaRepository, usuarios, senha);

        cadastro.cadastrarEscola(dtoValido(), superAdmin);

        var gestor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(gestor.capture(), eq(tenantId));
        assertEquals(tenantId, gestor.getValue().getTenantId());
        assertEquals(escolaId, gestor.getValue().getEscolaId());
    }

    private CriarEscolaDTO dtoValido() {
        CriarEscolaDTO dto = new CriarEscolaDTO();
        dto.setNome("Escola de Teste");
        dto.setCnpj("04.252.011/0001-10");
        dto.setEmailInstitucional("escola@test.com");
        dto.setEndereco("Rua de Teste, 10");
        dto.setBairro("Centro");
        dto.setCidade("Recife");
        dto.setEstado("PE");
        dto.setCep("50000-000");
        dto.setNomeResponsavel("Gestor de Teste");
        dto.setCpfResponsavel("529.982.247-25");
        dto.setTelefoneResponsavel("81999999999");
        dto.setEmailResponsavel("gestor@test.com");
        return dto;
    }
}
