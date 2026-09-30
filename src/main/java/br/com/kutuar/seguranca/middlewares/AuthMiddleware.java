package br.com.kutuar.seguranca.middlewares;

import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.services.JwtService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import br.com.kutuar.seguranca.utils.CookieUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Middleware que tenta autenticar a partir do cookie JWT.
 * Se o token for válido, popula AuthUserContext e ctx.attribute("currentUser").
 * Se o token for inválido/expirado, apenas remove o cookie e permite a requisição continuar.
 * A responsabilidade de bloquear/lançar AuthenticationException fica nas rotas protegidas.
 */
public class AuthMiddleware implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AuthMiddleware.class);
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;

    public AuthMiddleware(JwtService jwtService) {
        this(jwtService, new UsuarioRepository(), new EscolaRepository());
    }

    public AuthMiddleware(JwtService jwtService, UsuarioRepository usuarioRepository,
                          EscolaRepository escolaRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.escolaRepository = escolaRepository;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        // Limpa contexto sempre no início da requisição
        AuthUserContext.clear();

        String token = CookieUtil.getJwtToken(ctx);

        if (token != null) {
            try {
                AuthUser authUser = jwtService.extrairAuthUser(token);
                if (authUser != null && sessaoEstaAtiva(authUser)) {
                    AuthUserContext.setAuthUser(authUser);
                    ctx.attribute("currentUser", authUser); // Disponibiliza para templates e handlers
                    logger.debug("Usuário autenticado no contexto: userId={}, tenantId={}, perfil={}",
                            authUser.getUserId(), authUser.getTenantId(), authUser.getPerfil());
                } else {
                    logger.debug("Token JWT inválido, expirado ou sessão desativada. Removendo cookie e permitindo navegação pública.");
                    CookieUtil.removeJwtCookie(ctx);
                }
            } catch (Exception e) {
                // Em caso de erro inesperado no parsing do token, limpar cookie e continuar
                logger.warn("Falha ao processar token JWT; removendo cookie.");
                CookieUtil.removeJwtCookie(ctx);
            }
        }

        // Não lança AuthenticationException aqui — rotas protegidas devem verificar AuthUserContext.
    }

    private boolean sessaoEstaAtiva(AuthUser authUser) {
        if (authUser.getPerfil() == Perfil.SUPER_ADMIN) {
            return true;
        }

        if (authUser.getUserId() == null) {
            return false;
        }

        Optional<Usuario> usuarioAtual = usuarioRepository.findById(authUser.getUserId());
        if (usuarioAtual.isEmpty() || !usuarioAtual.get().isAtivo() || usuarioAtual.get().isBloqueado()) {
            logger.warn("Sessão negada para usuário inativo, bloqueado ou inexistente: {}", authUser.getUserId());
            return false;
        }

        Usuario usuario = usuarioAtual.get();
        if (usuario.getEscolaId() == null) {
            logger.warn("Sessão negada para usuário sem escola vinculada: {}", usuario.getId());
            return false;
        }

        Optional<Escola> escolaAtual = escolaRepository.findById(usuario.getEscolaId());
        if (escolaAtual.isEmpty() || !"ATIVA".equals(escolaAtual.get().getStatus())) {
            logger.warn("Sessão negada para escola inexistente ou inativa: usuarioId={}, escolaId={}",
                    usuario.getId(), usuario.getEscolaId());
            return false;
        }

        return true;
    }
}
