package br.com.synge.seguranca.services;

import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import br.com.synge.seguranca.utils.ValidationUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final int maxLoginAttempts;
    private final long lockoutDurationMinutes;

    public AuthService(UsuarioRepository usuarioRepository, PasswordService passwordService, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;

        Dotenv dotenv = Dotenv.load();
        this.maxLoginAttempts = Integer.parseInt(dotenv.get("MAX_LOGIN_ATTEMPTS", "5"));
        this.lockoutDurationMinutes = Long.parseLong(dotenv.get("LOCKOUT_DURATION_MINUTES", "30"));
    }

    public String autenticar(String cpf, String senha) {
        // 1. Validar formato do CPF
        ValidationUtil.validateCpf(cpf);

        // 2. Buscar usuário por CPF
        Optional<Usuario> optionalUsuario = usuarioRepository.findByCpf(cpf);
        if (optionalUsuario.isEmpty()) {
            logger.warn("Tentativa de login com CPF inexistente: {}", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"));
            throw new AuthenticationException("CPF ou senha inválidos.");
        }

        Usuario usuario = optionalUsuario.get();

        // 3. Verificar bloqueio de conta
        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(LocalDateTime.now())) {
            logger.warn("Tentativa de login em conta bloqueada para CPF: {}", usuario.getCpfMascarado());
            throw new AuthenticationException("Sua conta está bloqueada até " + usuario.getBloqueadoAte().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + ".");
        }

        // 4. Verificar senha
        if (!passwordService.verificar(senha, usuario.getSenha())) {
            registrarTentativaLoginFalha(usuario);
            logger.warn("Tentativa de login com senha inválida para CPF: {}", usuario.getCpfMascarado());
            throw new AuthenticationException("CPF ou senha inválidos.");
        }

        // 5. Login bem-sucedido: Resetar tentativas e gerar JWT
        resetarTentativasLogin(usuario);
        logger.info("Login bem-sucedido para usuário ID: {}", usuario.getId());
        return jwtService.gerarToken(usuario.getId(), usuario.getTenantId(), usuario.getPerfil(), usuario.getCpf());
    }

    private void registrarTentativaLoginFalha(Usuario usuario) {
        usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
        if (usuario.getTentativasLogin() >= maxLoginAttempts) {
            usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            logger.warn("Conta bloqueada para CPF: {}", usuario.getCpfMascarado());
        }
        usuarioRepository.update(usuario);
    }

    private void resetarTentativasLogin(Usuario usuario) {
        if (usuario.getTentativasLogin() > 0 || usuario.getBloqueadoAte() != null) {
            usuario.setTentativasLogin(0);
            usuario.setBloqueadoAte(null);
            usuarioRepository.update(usuario);
        }
    }

    // Método para criar um usuário inicial (ex: SUPER_ADMIN)
    public void criarUsuarioInicial(UUID tenantId, String nome, String cpf, String senha, Perfil perfil) {
        if (usuarioRepository.findByCpf(cpf).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setTenantId(tenantId);
            usuario.setNome(nome);
            usuario.setCpf(cpf);
            usuario.setSenha(passwordService.gerarHash(senha)); // Hash da senha
            usuario.setPerfil(perfil);
            usuario.setTentativasLogin(0);
            usuario.setAtivo(true); // Adicionar campo ativo ao modelo Usuario se necessário
            usuarioRepository.save(usuario);
            logger.info("Usuário inicial {} criado com sucesso para tenantId: {}", usuario.getCpfMascarado(), tenantId);
        } else {
            logger.info("Usuário inicial {} já existe.", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"));
        }
    }
}
