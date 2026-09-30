package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.AtualizarUsuarioDTO;
import br.com.kutuar.seguranca.dtos.AtualizarPerfilUsuarioDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class UsuarioAdminServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AuditoriaService auditoriaService;

    private UsuarioAdminService service;
    private UUID usuarioId;
    private UUID tenantId;
    private AuthUser superAdmin;

    @BeforeEach
    void setUp() {
        service = new UsuarioAdminService(usuarioRepository, auditoriaService);
        usuarioId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        superAdmin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "00000000000");
    }

    @Test
    void aceitaTelefoneVazioEpreservaValorExistente() {
        Usuario usuario = usuarioEscolar();
        prepararAtualizacao(usuario);
        AtualizarUsuarioDTO dto = dtoComTelefone("   ");

        service.atualizar(usuarioId, dto, superAdmin);

        assertEquals("(11)99999-9999", usuario.getTelefone());
        verify(usuarioRepository).updateCadastro(usuario);
    }

    @Test
    void normalizaTelefoneInformadoAntesDePersistir() {
        Usuario usuario = usuarioEscolar();
        prepararAtualizacao(usuario);
        AtualizarUsuarioDTO dto = dtoComTelefone(" (11) 99999-9999 ");

        service.atualizar(usuarioId, dto, superAdmin);

        assertEquals("(11)99999-9999", usuario.getTelefone());
    }

    @Test
    void rejeitaConflitoDeEmailCom409() {
        Usuario usuario = usuarioEscolar();
        prepararAtualizacao(usuario);
        Usuario outroUsuario = usuarioEscolar();
        outroUsuario.setId(UUID.randomUUID());
        when(usuarioRepository.findByEmail("outro@example.com")).thenReturn(Optional.of(outroUsuario));
        AtualizarUsuarioDTO dto = dtoComTelefone("(11)99999-9999");
        dto.setEmail("outro@example.com");

        assertThrows(ConflictException.class, () -> service.atualizar(usuarioId, dto, superAdmin));
        verify(usuarioRepository, never()).updateCadastro(any());
    }

    @Test
    void rejeitaConflitoDeCpfCom409() {
        Usuario usuario = usuarioEscolar();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        Usuario outroUsuario = usuarioEscolar();
        outroUsuario.setId(UUID.randomUUID());
        when(usuarioRepository.findByCpf("12345678909")).thenReturn(Optional.of(outroUsuario));
        AtualizarUsuarioDTO dto = dtoComTelefone("(11)99999-9999");
        dto.setCpf("12345678909");

        assertThrows(ConflictException.class, () -> service.atualizar(usuarioId, dto, superAdmin));
        verify(usuarioRepository, never()).updateCadastro(any());
    }

    @Test
    void rejeitaDadosInvalidosCom400() {
        AtualizarUsuarioDTO dto = dtoComTelefone("");
        dto.setNomeCompleto("Ana");

        assertThrows(ValidationException.class, () -> service.atualizar(usuarioId, dto, superAdmin));
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void rejeitaUsuarioInexistenteCom404() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.atualizar(usuarioId, dtoComTelefone(""), superAdmin));
        verify(usuarioRepository, never()).updateCadastro(any());
    }

    @Test
    void bloqueiaUsuarioDeOutroTenantCom403() {
        UUID gestorTenant = UUID.randomUUID();
        AuthUser gestor = new AuthUser(UUID.randomUUID(), gestorTenant, UUID.randomUUID(), Perfil.GESTOR, "11111111111");
        when(usuarioRepository.findById(usuarioId, gestorTenant)).thenReturn(Optional.empty());

        assertThrows(AuthorizationException.class, () -> service.atualizar(usuarioId, dtoComTelefone(""), gestor));
        verify(usuarioRepository, never()).updateCadastro(any());
    }

    @Test
    void bloqueiaPerfilSemPermissaoCom403() {
        AuthUser secretaria = new AuthUser(UUID.randomUUID(), tenantId, UUID.randomUUID(), Perfil.SECRETARIA, "11111111111");

        assertThrows(AuthorizationException.class, () -> service.atualizar(usuarioId, dtoComTelefone(""), secretaria));
        verify(usuarioRepository, never()).findById(any(), any());
    }

    @Test
    void superAdminAprovaUsuarioEGeraAuditoriaPersistente() {
        Usuario usuario = usuarioEscolar();
        usuario.setAtivo(false);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        service.aprovar(usuarioId, superAdmin);

        verify(usuarioRepository).approve(usuarioId);
        verify(auditoriaService).registrar(eq(superAdmin), eq(tenantId), eq("USUARIO_APROVADO"),
                eq("USUARIO"), eq(usuarioId), eq("status=ATIVO"));
    }

    @Test
    void superAdminAlteraPerfilEGeraAuditoriaPersistente() {
        Usuario usuario = usuarioEscolar();
        AtualizarPerfilUsuarioDTO dto = new AtualizarPerfilUsuarioDTO();
        dto.setPerfil(Perfil.PROFESSOR);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        service.alterarPerfil(usuarioId, dto, superAdmin);

        verify(usuarioRepository).updatePerfilETenant(usuario);
        verify(auditoriaService).registrar(eq(superAdmin), eq(tenantId), eq("USUARIO_PERFIL_ALTERADO"),
                eq("USUARIO"), eq(usuarioId), eq("perfil_anterior=GESTOR;perfil_novo=PROFESSOR"));
    }

    private void prepararAtualizacao(Usuario usuario) {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCpf(any())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
    }

    private Usuario usuarioEscolar() {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setTenantId(tenantId);
        usuario.setEscolaId(UUID.randomUUID());
        usuario.setNomeCompleto("Maria da Silva");
        usuario.setEmail("maria@example.com");
        usuario.setCpf("52998224725");
        usuario.setTelefone("(11)99999-9999");
        usuario.setPerfil(Perfil.GESTOR);
        usuario.setAtivo(true);
        return usuario;
    }

    private AtualizarUsuarioDTO dtoComTelefone(String telefone) {
        AtualizarUsuarioDTO dto = new AtualizarUsuarioDTO();
        dto.setNomeCompleto("Maria da Silva");
        dto.setEmail("maria@example.com");
        dto.setCpf("52998224725");
        dto.setTelefone(telefone);
        return dto;
    }
}
