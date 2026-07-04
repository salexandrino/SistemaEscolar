package br.com.synge.seguranca.services;

import br.com.synge.seguranca.dtos.AtualizarPerfilUsuarioDTO;
import br.com.synge.seguranca.dtos.AtualizarUsuarioDTO;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.exceptions.AuthorizationException;
import br.com.synge.seguranca.exceptions.BusinessException;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import br.com.synge.seguranca.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioAdminService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioAdminService.class);
    private final UsuarioRepository usuarioRepository;

    public UsuarioAdminService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        if (isMaster(currentUser)) {
            return usuarioRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
        }
        validarAdministradorEscola(currentUser);
        return buscarUsuarioDoTenantOuNegar(id, currentUser.getTenantId());
    }

    public Usuario atualizar(UUID id, AtualizarUsuarioDTO dto, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        validarDtoAtualizacao(dto);

        Usuario usuario = buscarParaAlteracao(id, currentUser);
        validarConflitos(usuario, dto, currentUser);

        if (dto.getEscolaId() != null && !isMaster(currentUser) && !dto.getEscolaId().equals(currentUser.getTenantId())) {
            throw new AuthorizationException("Usuario pertence a outro tenant.");
        }

        usuario.setEscolaId(dto.getEscolaId() != null ? dto.getEscolaId() : usuario.getEscolaId());
        usuario.setNomeCompleto(dto.getNomeCompleto().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setCpf(dto.getCpf().trim());
        usuario.setTelefone(dto.getTelefone().trim());

        usuarioRepository.updateCadastro(usuario);
        logger.info("Usuario ID {} atualizado administrativamente.", id);
        return buscarPorId(id, currentUser);
    }

    public void aprovar(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        Usuario usuario = buscarParaAlteracao(id, currentUser);
        if (usuario.isAtivo()) {
            throw new BusinessException("Usuario ja esta ativo.");
        }

        if (isMaster(currentUser)) {
            usuarioRepository.approve(id);
        } else {
            usuarioRepository.approve(id, currentUser.getTenantId());
        }
        logger.info("Usuario ID {} aprovado administrativamente.", id);
    }

    public void alterarPerfil(UUID id, AtualizarPerfilUsuarioDTO dto, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        if (!isMaster(currentUser)) {
            throw new AuthorizationException("Apenas o Administrador Master pode alterar perfil de usuario.");
        }
        if (dto == null || dto.getPerfil() == null) {
            throw new ValidationException("Perfil e obrigatorio.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
        usuarioRepository.updateProfile(usuario.getId(), usuario.getTenantId(), dto.getPerfil());
        logger.info("Perfil do usuario ID {} atualizado administrativamente.", id);
    }

    public void inativar(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        Usuario usuario = buscarParaAlteracao(id, currentUser);
        if (!usuario.isAtivo()) {
            throw new BusinessException("Usuario ja esta inativo.");
        }
        usuarioRepository.inactivate(usuario.getId(), usuario.getTenantId());
        logger.info("Usuario ID {} inativado administrativamente.", id);
    }

    private Usuario buscarParaAlteracao(UUID id, AuthUser currentUser) {
        if (isMaster(currentUser)) {
            return usuarioRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
        }
        validarAdministradorEscola(currentUser);
        return buscarUsuarioDoTenantOuNegar(id, currentUser.getTenantId());
    }

    private Usuario buscarUsuarioDoTenantOuNegar(UUID id, UUID tenantId) {
        return usuarioRepository.findById(id, tenantId)
                .orElseThrow(() -> new AuthorizationException("Usuario pertence a outro tenant."));
    }

    private void validarDtoAtualizacao(AtualizarUsuarioDTO dto) {
        if (dto == null) {
            throw new ValidationException("Dados do usuario sao obrigatorios.");
        }
        ValidationUtil.validateNomeCompleto(dto.getNomeCompleto());
        ValidationUtil.validateEmail(dto.getEmail());
        ValidationUtil.validateCpf(dto.getCpf());
        ValidationUtil.validateTelefone(dto.getTelefone());
    }

    private void validarConflitos(Usuario usuario, AtualizarUsuarioDTO dto, AuthUser currentUser) {
        Optional<Usuario> usuarioComCpf = isMaster(currentUser)
                ? usuarioRepository.findByCpf(dto.getCpf())
                : usuarioRepository.findByCpf(dto.getCpf(), currentUser.getTenantId());
        if (usuarioComCpf.isPresent() && !usuarioComCpf.get().getId().equals(usuario.getId())) {
            throw new ConflictException("CPF ja cadastrado.");
        }

        Optional<Usuario> usuarioComEmail = isMaster(currentUser)
                ? usuarioRepository.findByEmail(dto.getEmail())
                : usuarioRepository.findByEmail(dto.getEmail(), currentUser.getTenantId());
        if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(usuario.getId())) {
            throw new ConflictException("E-mail ja cadastrado.");
        }
    }

    private void validarUsuarioAutenticado(AuthUser currentUser) {
        if (currentUser == null) {
            throw new AuthenticationException("Usuario nao autenticado.");
        }
    }

    private void validarAdministradorEscola(AuthUser currentUser) {
        if (currentUser.getPerfil() != Perfil.GESTOR) {
            throw new AuthorizationException("Acesso negado para gerenciar usuarios.");
        }
    }

    private boolean isMaster(AuthUser currentUser) {
        return currentUser.getPerfil() == Perfil.SUPER_ADMIN;
    }


}
