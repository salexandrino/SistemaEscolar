package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.AlterarSenhaPropriaDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.*;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.utils.ValidationUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditoriaService auditoriaService;
    private final UsuarioTenantService usuarioTenantService = new UsuarioTenantService();
    private final int maxLoginAttempts;
    private final long lockoutDurationMinutes;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RECOVERY_TOKEN_BYTES = 32;
    private static final long RECOVERY_TOKEN_EXPIRATION_MINUTES = 15;

    public AuthService(UsuarioRepository usuarioRepository, EscolaRepository escolaRepository, PasswordService passwordService, JwtService jwtService, EmailService emailService) {
        this(usuarioRepository, escolaRepository, passwordService, jwtService, emailService, AuditoriaService.semPersistencia());
    }

    public AuthService(UsuarioRepository usuarioRepository, EscolaRepository escolaRepository, PasswordService passwordService,
                       JwtService jwtService, EmailService emailService, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.escolaRepository = escolaRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.auditoriaService = auditoriaService == null ? AuditoriaService.semPersistencia() : auditoriaService;

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String maxAttempts = System.getenv("MAX_LOGIN_ATTEMPTS");
        if (maxAttempts == null || maxAttempts.isBlank()) {
            maxAttempts = dotenv.get("MAX_LOGIN_ATTEMPTS", "5");
        }
        this.maxLoginAttempts = Integer.parseInt(maxAttempts);

        String lockoutMinutes = System.getenv("LOCKOUT_DURATION_MINUTES");
        if (lockoutMinutes == null || lockoutMinutes.isBlank()) {
            lockoutMinutes = dotenv.get("LOCKOUT_DURATION_MINUTES", "30");
        }
        this.lockoutDurationMinutes = Long.parseLong(lockoutMinutes);
    }

    public String autenticar(String cpf, String senha) {
        ValidationUtil.validarCpfComStrategy(cpf);
        // Mesma normalização usada ao salvar (Usuario.setCpf): sem isso, um CPF
        // digitado/mascarado de forma diferente da que foi gravado nunca dá match.
        String cpfNormalizado = ValidationUtil.extractNumbers(cpf);

        Optional<Usuario> optionalUsuario = usuarioRepository.findByCpf(cpfNormalizado);
        if (optionalUsuario.isEmpty()) {
            logger.warn("Login negado: credenciais não reconhecidas.");
            throw new AuthenticationException("CPF não cadastrado. Por favor, crie uma conta para continuar.");
        }

        Usuario usuario = optionalUsuario.get();

        if (!usuario.isAtivo()) { // Verifica se o usuário está ativo
            logger.warn("Login negado: conta inativa ou pendente. usuarioId={}", usuario.getId());
            throw new AuthenticationException("Sua conta ainda não foi aprovada pelo Gestor.");
        }

        if (usuario.isBloqueado()) { // Verifica se o usuário está bloqueado
            logger.warn("Login negado: conta bloqueada. usuarioId={}", usuario.getId());
            throw new AuthenticationException("Sua conta está bloqueada. Entre em contato com o administrador.");
        }

        if (!passwordService.verificar(senha, usuario.getSenhaHash())) {
            registrarTentativaLoginFalha(usuario);
            logger.warn("Login negado: credenciais inválidas. usuarioId={}", usuario.getId());
            throw new AuthenticationException("CPF ou senha inválidos.");
        }

        validarEscolaAtiva(usuario);

        resetarTentativasLogin(usuario);
        usuario.setUltimoLogin(LocalDateTime.now()); // Atualiza último login
        usuarioRepository.update(usuario); // Persiste a atualização do último login
        logger.info("Login bem-sucedido para usuário ID: {}", usuario.getId());
        return jwtService.gerarToken(
                usuario.getId(),
                usuario.getTenantId(),
                usuario.getEscolaId(),
                usuario.getPerfil(),
                usuario.getCpf()
        );
    }

    private void validarEscolaAtiva(Usuario usuario) {
        if (usuario.getPerfil() == Perfil.SUPER_ADMIN) {
            return;
        }

        if (usuario.getEscolaId() == null) {
            logger.warn("Tentativa de login de usuário sem escola vinculada: {}", usuario.getId());
            throw new AuthenticationException("Não foi possível autenticar sua conta.");
        }

        Escola escola = escolaRepository.findById(usuario.getEscolaId())
                .orElseThrow(() -> new AuthenticationException("Não foi possível autenticar sua conta."));
        if (!"ATIVA".equals(escola.getStatus())) {
            logger.warn("Tentativa de login em escola inativa: usuarioId={}, escolaId={}",
                    usuario.getId(), escola.getId());
            throw new AuthenticationException("O acesso desta escola está temporariamente indisponível.");
        }
    }

    private void registrarTentativaLoginFalha(Usuario usuario) {
        usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
        if (usuario.getTentativasLogin() >= maxLoginAttempts) {
            usuario.setBloqueado(true); // Marca como bloqueado
            // usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(lockoutDurationMinutes)); // Se quiser bloqueio temporário
            logger.warn("Conta bloqueada após exceder tentativas de login. usuarioId={}", usuario.getId());
        }
        usuarioRepository.update(usuario);
    }

    private void resetarTentativasLogin(Usuario usuario) {
        if (usuario.getTentativasLogin() > 0 || usuario.isBloqueado()) {
            usuario.setTentativasLogin(0);
            usuario.setBloqueado(false);
            // usuario.setBloqueadoAte(null);
            usuarioRepository.update(usuario);
        }
    }
    public void register(Usuario usuario) {

        // 🛡️ Validação preventiva de nulos antes de efetuar operações de string
        if (usuario == null) {
            throw new ValidationException("Dados de cadastro inválidos.");
        }
        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().isBlank()) {
            throw new ValidationException("A senha é obrigatória.");
        }
        if (usuario.getNomeCompleto() == null || usuario.getNomeCompleto().isBlank()) {
            throw new ValidationException("O nome completo é obrigatório.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new ValidationException("O e-mail é obrigatório.");
        }

        usuario.setNomeCompleto(usuario.getNomeCompleto().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setCpf(usuario.getCpf() != null ? usuario.getCpf().trim() : "");
        usuario.setTelefone(usuario.getTelefone() != null ? usuario.getTelefone().trim() : "");

        ValidationUtil.validateNomeCompleto(usuario.getNomeCompleto());
        ValidationUtil.validateEmail(usuario.getEmail());
        ValidationUtil.validarCpfComStrategy(usuario.getCpf());

        if (!usuario.getSenhaHash().equals(usuario.getConfirmacaoSenha())) {
            throw new ValidationException("Senha e confirmação de senha não conferem.");
        }

        if (usuarioRepository.existsByCpf(usuario.getCpf(), usuario.getTenantId())) {
            logger.warn("Cadastro de usuário negado: CPF já cadastrado.");
            throw new ConflictException("CPF já cadastrado.");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail(), usuario.getTenantId())) {
            logger.warn("Cadastro de usuário negado: e-mail já cadastrado.");
            throw new ConflictException("E-mail já cadastrado.");
        }

        // Perfil
        if (usuario.getPerfil() == Perfil.SUPER_ADMIN) {
            throw new AuthorizationException("Não é permitido cadastrar usuários com o perfil " + usuario.getPerfil().name() + " via este endpoint.");
        }
        // GESTOR nasce junto com a escola (EscolaService.cadastrarEscola), nunca via
        // auto-cadastro público — evita que qualquer pessoa vire administradora de
        // uma escola que não é dela.
        if (usuario.getPerfil() == Perfil.GESTOR) {
            throw new AuthorizationException("O perfil de Gestor é criado automaticamente no cadastro da escola e não está disponível para auto-cadastro.");
        }

        usuarioTenantService.validarConsistencia(usuario);

        // Escola
        if (usuario.getEscolaId() == null) {
            throw new ValidationException("Escola é obrigatória.");
        }
        Optional<Escola> escolaOptional = escolaRepository.findById(usuario.getEscolaId());
        if (escolaOptional.isEmpty() || !"ATIVA".equals(escolaOptional.get().getStatus())) {
            throw new NotFoundException("Escola não encontrada ou inativa.");
        }

        usuario.setSenhaHash(passwordService.hash(usuario.getSenhaHash()));

        // 🔐 Todo auto-cadastro público nasce pendente de aprovação por um Gestor
        // da escola (GESTOR e SUPER_ADMIN já foram bloqueados acima e nunca chegam aqui).
        usuario.setAtivo(false);
        usuario.setBloqueado(false);
        usuario.setTentativasLogin(0);
        usuario.setCriadoEm(LocalDateTime.now());
        usuario.setAtualizadoEm(LocalDateTime.now());

        usuarioRepository.save(usuario, usuario.getTenantId());

        if (usuario.isAtivo()) {
            logger.info("Usuário cadastrado e ativo. usuarioId={}", usuario.getId());
        } else {
            logger.info("Usuário cadastrado com aprovação pendente. usuarioId={}", usuario.getId());
        }
    }
    public void approveUser(UUID userId, UUID tenantId, AuthUser approver) {
        // 1. Verificar se o aprovador tem permissão (GESTOR)
        if (approver.getPerfil() != Perfil.GESTOR) {
            logger.warn("Tentativa de aprovação de usuário por perfil não autorizado: {}", approver.getPerfil());
            throw new AuthorizationException("Somente Gestores podem aprovar usuários.");
        }

        // 2. Buscar usuário a ser aprovado
        Optional<Usuario> optionalUsuario = usuarioRepository.findByIdAndTenantId(userId, tenantId);
        if (optionalUsuario.isEmpty()) {
            throw new NotFoundException("Usuário não encontrado ou não pertence à sua escola.");
        }
        Usuario usuario = optionalUsuario.get();

        // 3. Verificar se o usuário já está ativo
        if (usuario.isAtivo()) {
            throw new BusinessException("Usuário já está ativo.");
        }

        // 4. Aprovar usuário
        usuarioRepository.approve(userId, tenantId);
        logger.info("Usuário aprovado por gestor. usuarioId={}, executorId={}, tenantId={}",
                userId, approver.getUserId(), tenantId);
    }

    public void forgotPassword(String email) {
        ValidationUtil.validateEmail(email);

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email.trim().toLowerCase());

        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            String recoveryToken = gerarTokenRecuperacao();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RECOVERY_TOKEN_EXPIRATION_MINUTES);

            usuario.setResetPasswordToken(BCrypt.hashpw(recoveryToken, BCrypt.gensalt()));
            usuario.setResetPasswordExpiresAt(expiresAt);

            try {
                usuarioRepository.update(usuario);
                logger.info("Solicitação de recuperação de senha processada. usuarioId={}", usuario.getId());
                emailService.enviarCodigoRecuperacaoSenha(usuario.getEmail(), usuario.getNomeCompleto(), recoveryToken);
            } catch (Exception e) {
                logger.error("Falha ao processar recuperação de senha. usuarioId={}, tipo={}",
                        usuario.getId(), e.getClass().getSimpleName());
                throw new InternalServerException("Erro interno ao gerar código de recuperação.");
            }
        } else {
            logger.info("Solicitação de recuperação de senha processada sem conta elegível.");
            // Não revelar que o e-mail não existe por segurança — apenas registrar no log
        }
    }

    public void resetPassword(String email, String codigo, String novaSenha, String confirmacaoSenha) {
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePasswordComplexity(novaSenha);

        if (!novaSenha.equals(confirmacaoSenha)) {
            throw new ValidationException("Nova senha e confirmação de senha não conferem.");
        }

        if (codigo == null || codigo.isBlank()) {
            throw new ValidationException("Código de recuperação inválido.");
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (optionalUsuario.isEmpty()) {
            throw new ValidationException("Código de recuperação inválido.");
        }

        Usuario usuario = optionalUsuario.get();

        if (usuario.getResetPasswordToken() == null || !tokenRecuperacaoConfere(codigo, usuario.getResetPasswordToken())) {
            throw new ValidationException("Código de recuperação inválido.");
        }
        if (usuario.getResetPasswordExpiresAt() == null || !usuario.getResetPasswordExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Código de recuperação expirado.");
        }

        // Atualiza a senha e limpa o token de recuperação
        String newPasswordHash = passwordService.hash(novaSenha);
        usuarioRepository.updatePassword(usuario.getId(), usuario.getTenantId(), newPasswordHash);
        auditarSenha(usuario, "SENHA_REDEFINIDA_POR_RECUPERACAO", "origem=RECUPERACAO_SENHA");

        logger.info("Senha redefinida com sucesso para usuário ID {}.", usuario.getId());
    }

    private String gerarTokenRecuperacao() {
        byte[] bytes = new byte[RECOVERY_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean tokenRecuperacaoConfere(String token, String tokenHash) {
        try {
            return BCrypt.checkpw(token, tokenHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String autenticarSuperAdmin(String email, String senha) {

        logger.info("Tentativa de login do Super Admin recebida.");

        ValidationUtil.validateEmail(email);

        // Busca o usuário apenas UMA vez no banco para economizar performance
        Usuario usuario = usuarioRepository.findSuperAdminByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Login do Super Admin negado: credenciais não reconhecidas.");
                    return new AuthenticationException("Email ou senha inválidos.");
                });

        // Verifica se a senha bate com o hash criptografado (Sem printar nada no console!)
        boolean senhaCorreta = passwordService.verificar(senha, usuario.getSenhaHash());

        if (!senhaCorreta) {
            logger.warn("Login do Super Admin negado: credenciais inválidas. usuarioId={}", usuario.getId());
            throw new AuthenticationException("Email ou senha inválidos.");
            // Dica: Use uma mensagem genérica para não dar pistas a invasores se o e-mail ou a senha estavam certos
        }

        logger.info("Login do Super Admin concluído. usuarioId={}", usuario.getId());

        // Gera o token JWT com segurança
        String token = jwtService.gerarToken(
                usuario.getId(),
                null,
                null,
                usuario.getPerfil(),
                usuario.getCpf()
        );

        return token;
    }

    public void alterarSenhaPropria(UUID usuarioId, UUID tenantId, AlterarSenhaPropriaDTO dto) {
        if (!dto.getNovaSenha().equals(dto.getConfirmacaoNovaSenha())) {
            throw new ValidationException("As senhas não conferem.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        if (!passwordService.verificar(dto.getSenhaAtual(), usuario.getSenhaHash())) {
            throw new AuthorizationException("Senha atual incorreta.");
        }

        ValidationUtil.validatePasswordComplexity(dto.getNovaSenha());

        String novoHash = passwordService.hash(dto.getNovaSenha());
        usuario.setSenhaHash(novoHash);

        // O prompt pede "persiste via usuarioRepository.updateCadastro(usuario)" mas esse método
        // atualiza a data e outros campos, e especificamente a senha? Espera, o updateCadastro atualiza
        // (escola_id, nome_completo, email, cpf, telefone, atualizado_em). Não atualiza senha_hash!
        // No passo anterior vimos o repositório. O repositório tem updatePassword(UUID id, UUID tenantId, String newPasswordHash).
        // Vou usar o updatePassword do repository que é feito para isso.
        usuarioRepository.updatePassword(usuarioId, tenantId, novoHash);
        auditarSenha(usuario, "SENHA_PROPRIA_ALTERADA", "origem=ALTERACAO_PROPRIA");
        
        logger.info("Usuário {} alterou a própria senha com sucesso.", usuarioId);
    }

    private void auditarSenha(Usuario usuario, String acao, String detalhes) {
        AuthUser executor = new AuthUser(usuario.getId(), usuario.getTenantId(), usuario.getEscolaId(),
                usuario.getPerfil(), usuario.getCpf());
        auditoriaService.registrar(executor, usuario.getTenantId(), acao, "USUARIO", usuario.getId(), detalhes);
    }
}
