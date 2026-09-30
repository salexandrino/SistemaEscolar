package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.AtualizarPerfilUsuarioDTO;
import br.com.kutuar.seguranca.dtos.AtualizarUsuarioDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.BusinessException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioAdminService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioAdminService.class);
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioTenantService usuarioTenantService = new UsuarioTenantService();

    public UsuarioAdminService(UsuarioRepository usuarioRepository) {
        this(usuarioRepository, AuditoriaService.semPersistencia());
    }

    public UsuarioAdminService(UsuarioRepository usuarioRepository, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService == null ? AuditoriaService.semPersistencia() : auditoriaService;
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
        if (dto != null) {
            dto.setTelefone(normalizarTelefone(dto.getTelefone()));
        }
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
        if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
            usuario.setTelefone(dto.getTelefone());
        }

        usuarioTenantService.validarConsistencia(usuario);
        usuarioRepository.updateCadastro(usuario);
        auditarSeSuperAdmin(currentUser, usuario.getTenantId(), "USUARIO_EDITADO", usuario.getId(), "campos=cadastro");
        logger.info("Usuario ID {} updated administrativamente.", id);
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
        auditarSeSuperAdmin(currentUser, usuario.getTenantId(), "USUARIO_APROVADO", usuario.getId(), "status=ATIVO");
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
        Perfil perfilAnterior = usuario.getPerfil();
        usuarioTenantService.validarTrocaDePerfil(usuario, dto.getPerfil());
        usuarioRepository.updatePerfilETenant(usuario);
        auditarSeSuperAdmin(currentUser, usuario.getTenantId(), "USUARIO_PERFIL_ALTERADO", usuario.getId(),
                "perfil_anterior=" + perfilAnterior + ";perfil_novo=" + usuario.getPerfil());
        logger.info("Perfil do usuario ID {} atualizado administrativamente.", id);
    }

    /**
     * Desbloqueia uma conta que foi bloqueada automaticamente após
     * exceder o número máximo de tentativas de login (ver AuthService).
     * Sem este método não havia NENHUMA forma de reverter o bloqueio a
     * não ser alterando o banco de dados diretamente.
     */
    public void desbloquear(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        Usuario usuario = buscarParaAlteracao(id, currentUser);

        if (!usuario.isBloqueado()) {
            throw new BusinessException("Usuario nao esta bloqueado.");
        }

        usuario.setBloqueado(false);
        usuario.setTentativasLogin(0);
        usuarioRepository.update(usuario);
        auditarSeSuperAdmin(currentUser, usuario.getTenantId(), "USUARIO_DESBLOQUEADO", usuario.getId(), "tentativas_login=0");
        logger.info("Usuario ID {} desbloqueado administrativamente por {}.", id, currentUser.getCpf());
    }

    public void inativar(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        Usuario usuario = buscarParaAlteracao(id, currentUser);
        if (!usuario.isAtivo()) {
            throw new BusinessException("Usuario ja esta inativo.");
        }
        usuarioRepository.inactivate(usuario.getId(), usuario.getTenantId());
        auditarSeSuperAdmin(currentUser, usuario.getTenantId(), "USUARIO_INATIVADO", usuario.getId(), "status=INATIVO");
        logger.info("Usuario ID {} inativado administrativamente.", id);
    }

    /**
     * Reverte a inativação. Sem este método, uma vez inativado o usuário
     * nunca mais poderia ser reativado pela tela/API — só mexendo direto no banco.
     */
    public void reativar(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        Usuario usuario = buscarParaAlteracao(id, currentUser);
        if (usuario.isAtivo()) {
            throw new BusinessException("Usuario ja esta ativo.");
        }
        usuarioRepository.activate(usuario.getId(), usuario.getTenantId());
        auditarSeSuperAdmin(currentUser, usuario.getTenantId(), "USUARIO_REATIVADO", usuario.getId(), "status=ATIVO");
        logger.info("Usuario ID {} reativado administrativamente.", id);
    }

    /**
     * Exclui de fato o usuário (hard delete) — diferente de inativar(), que só
     * marca ativo=false. Só permitido para usuário já inativo, como trava de
     * segurança: evita apagar por engano uma conta em uso, sem antes ter
     * passado pela etapa de inativação (que ainda é reversível).
     */
    public void excluir(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        Usuario usuario = buscarParaAlteracao(id, currentUser);

        if (currentUser.getUserId().equals(id)) {
            throw new BusinessException("Você não pode excluir o próprio usuário.");
        }
        if (usuario.isAtivo()) {
            throw new BusinessException("Só é possível excluir definitivamente um usuário que já esteja inativo. Inative-o primeiro.");
        }

        usuarioRepository.deleteHard(usuario.getId(), usuario.getTenantId());
        logger.info("Usuario ID {} excluido definitivamente por {}.", id, currentUser.getCpf());
    }

    /**
     * Exclui um usuário (soft delete: inativa a conta permanentemente).
     * SUPER_ADMIN pode excluir qualquer usuário.
     * GESTOR só pode excluir usuários do seu próprio tenant.
     */
    public void deletar(UUID id, AuthUser currentUser) {
        validarUsuarioAutenticado(currentUser);
        Usuario usuario = buscarParaAlteracao(id, currentUser);

        if (!usuario.isAtivo()) {
            throw new BusinessException("Usuário já está inativo.");
        }

        usuarioRepository.inactivate(usuario.getId(), usuario.getTenantId());
        logger.info("Usuario ID {} excluído (soft delete) por {}.", id, currentUser.getCpf());
    }

    private Usuario buscarParaAlteracao(UUID id, AuthUser currentUser) {
        if (isMaster(currentUser)) {
            return usuarioRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
        }
        validarAdministradorEscola(currentUser);
        return buscarUsuarioDoTenantOuNegar(id, currentUser.getTenantId());
    }

    private void auditarSeSuperAdmin(AuthUser executor, UUID tenantId, String acao, UUID entidadeId, String detalhes) {
        if (isMaster(executor)) {
            auditoriaService.registrar(executor, tenantId, acao, "USUARIO", entidadeId, detalhes);
        }
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
        ValidationUtil.validarCpfComStrategy(dto.getCpf());
        if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
            ValidationUtil.validateTelefone(dto.getTelefone());
        }
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }

        String valor = telefone.trim().replaceAll("\\s+", "");
        if (valor.isBlank()) {
            return "";
        }

        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() == 11) {
            return "(%s)%s-%s".formatted(digitos.substring(0, 2), digitos.substring(2, 7), digitos.substring(7));
        }
        return valor;
    }

    private void validarConflitos(Usuario usuario, AtualizarUsuarioDTO dto, AuthUser currentUser) {
        String cpfNormalizado = ValidationUtil.extractNumbers(dto.getCpf());
        Optional<Usuario> usuarioComCpf = isMaster(currentUser)
                ? usuarioRepository.findByCpf(cpfNormalizado)
                : usuarioRepository.findByCpf(cpfNormalizado, currentUser.getTenantId());
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
