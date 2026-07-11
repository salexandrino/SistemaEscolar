package br.com.synge.seguranca.services;

import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.*;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.repositories.EscolaRepository;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import br.com.synge.seguranca.utils.ValidationUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final int maxLoginAttempts;
    private final long lockoutDurationMinutes;
    private final Random random = new Random();

    public AuthService(UsuarioRepository usuarioRepository, EscolaRepository escolaRepository, PasswordService passwordService, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.escolaRepository = escolaRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;

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

        Optional<Usuario> optionalUsuario = usuarioRepository.findByCpf(cpf);
        if (optionalUsuario.isEmpty()) {
            logger.warn("Tentativa de login com CPF inexistente: {}", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"));
            throw new AuthenticationException("CPF não cadastrado. Por favor, crie uma conta para continuar.");
        }

        Usuario usuario = optionalUsuario.get();

        if (!usuario.isAtivo()) { // Verifica se o usuário está ativo
            logger.warn("Tentativa de login de usuário inativo/pendente: {}", usuario.getCpfMascarado());
            throw new AuthenticationException("Sua conta ainda não foi aprovada pelo Gestor.");
        }

        if (usuario.isBloqueado()) { // Verifica se o usuário está bloqueado
            logger.warn("Tentativa de login em conta bloqueada para CPF: {}", usuario.getCpfMascarado());
            throw new AuthenticationException("Sua conta está bloqueada. Entre em contato com o administrador.");
        }

        if (!passwordService.verificar(senha, usuario.getSenhaHash())) {
            registrarTentativaLoginFalha(usuario);
            logger.warn("Tentativa de login com senha inválida para CPF: {}", usuario.getCpfMascarado());
            throw new AuthenticationException("CPF ou senha inválidos.");
        }

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

    private void registrarTentativaLoginFalha(Usuario usuario) {
        usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
        if (usuario.getTentativasLogin() >= maxLoginAttempts) {
            usuario.setBloqueado(true); // Marca como bloqueado
            // usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(lockoutDurationMinutes)); // Se quiser bloqueio temporário
            logger.warn("Conta bloqueada para CPF: {}", usuario.getCpfMascarado());
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
            logger.warn("Tentativa de registro com CPF duplicado: {}", usuario.getCpfMascarado());
            throw new ConflictException("CPF já cadastrado.");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail(), usuario.getTenantId())) {
            logger.warn("Tentativa de registro com e-mail duplicado: {}", usuario.getEmailMascarado());
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
            logger.info("Usuário {} cadastrado e ATIVO pronto para uso.", usuario.getCpfMascarado());
        } else {
            logger.info("Usuário {} cadastrado com sucesso. Status: PENDENTE_APROVACAO.", usuario.getCpfMascarado());
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
        logger.info("Usuário ID {} aprovado por Gestor {} (Tenant {}).", userId, approver.getCpf(), tenantId);
    }

    public void forgotPassword(String email) {
        ValidationUtil.validateEmail(email);

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);

        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            String recoveryCode = String.format("%06d", random.nextInt(999999));
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

            usuario.setResetPasswordToken(recoveryCode);
            usuario.setResetPasswordExpiresAt(expiresAt);

            try {
                usuarioRepository.update(usuario);
                logger.info("Código de recuperação gerado para usuário {}. Código: {}", usuario.getEmail(), recoveryCode);
                // Em ambiente de desenvolvimento, imprimimos o código no console para testes
                System.out.println("CÓDIGO DE RECUPERAÇÃO PARA " + usuario.getEmail() + ": " + recoveryCode);
            } catch (Exception e) {
                logger.error("Erro ao salvar token de recuperação para {}: {}", usuario.getEmail(), e.getMessage(), e);
                throw new InternalServerException("Erro interno ao gerar código de recuperação.");
            }
        } else {
            logger.warn("Tentativa de recuperação de senha para e-mail não existente: {}", email);
            // Não revelar que o e-mail não existe por segurança — apenas registrar no log
        }
    }

    public void resetPassword(String email, String codigo, String novaSenha, String confirmacaoSenha) {
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePasswordComplexity(novaSenha);

        if (!novaSenha.equals(confirmacaoSenha)) {
            throw new ValidationException("Nova senha e confirmação de senha não conferem.");
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
        if (optionalUsuario.isEmpty()) {
            throw new NotFoundException("Usuário não encontrado."); // Mensagem genérica para segurança
        }

        Usuario usuario = optionalUsuario.get();

        if (usuario.getResetPasswordToken() == null || !usuario.getResetPasswordToken().equals(codigo)) {
            throw new ValidationException("Código de recuperação inválido.");
        }
        if (usuario.getResetPasswordExpiresAt() == null || usuario.getResetPasswordExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Código de recuperação expirado.");
        }

        // Atualiza a senha e limpa o token de recuperação
        String newPasswordHash = passwordService.hash(novaSenha);
        usuarioRepository.updatePassword(usuario.getId(), usuario.getTenantId(), newPasswordHash);

        logger.info("Senha do usuário {} redefinida com sucesso.", usuario.getEmail());
    }

    public String autenticarSuperAdmin(String email, String senha) {

        logger.info("Iniciando tentativa de login para o Super Admin: {}", email);

        ValidationUtil.validateEmail(email);

        // Busca o usuário apenas UMA vez no banco para economizar performance
        Usuario usuario = usuarioRepository.findSuperAdminByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Email ou senha inválidos."));

        // Verifica se a senha bate com o hash criptografado (Sem printar nada no console!)
        boolean senhaCorreta = passwordService.verificar(senha, usuario.getSenhaHash());

        if (!senhaCorreta) {
            logger.warn("Tentativa de login falhou: Senha incorreta para o Super Admin: {}", email);
            throw new AuthenticationException("Email ou senha inválidos.");
            // Dica: Use uma mensagem genérica para não dar pistas a invasores se o e-mail ou a senha estavam certos
        }

        logger.info("Super Admin [{}] autenticado com sucesso.", email);

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
}