package br.com.kutuar.seguranca;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.enums.Permissao;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.middlewares.AuthorizationMiddleware;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.services.TenantAccessGuard;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RbacAndTenantPolicyTest {

    private final UUID tenantA = UUID.randomUUID();
    private final UUID tenantB = UUID.randomUUID();
    private final Context context = mock(Context.class);

    @AfterEach
    void limpaAutenticacao() {
        AuthUserContext.clear();
    }

    @Test
    void rotaProtegidaSemAutenticacaoRetorna401() {
        assertThrows(AuthenticationException.class,
                () -> rotaLocal(Permissao.ALUNO_VISUALIZAR, tenantA, new AtomicBoolean()).handle(context));
    }

    @Test
    void superAdminAcessaEscolasUsuariosEAuditoriaGlobais() throws Exception {
        autenticar(Perfil.SUPER_ADMIN, null);

        assertGlobalPermission(Permissao.ESCOLA_VISUALIZAR);
        assertGlobalPermission(Permissao.USUARIO_VISUALIZAR);
        assertGlobalPermission(Permissao.AUDITORIA_VISUALIZAR);
    }

    @Test
    void gestorAcessaAlunoDaPropriaEscola() {
        autenticar(Perfil.GESTOR, tenantA);

        assertLocalPermission(Permissao.ALUNO_EDITAR);
    }

    @Test
    void gestorNaoAcessaAlunoDeOutraEscola() {
        autenticar(Perfil.GESTOR, tenantA);

        assertThrows(NotFoundException.class,
                () -> executar(rotaLocal(Permissao.ALUNO_VISUALIZAR, tenantB, new AtomicBoolean())));
    }

    @Test
    void gestorNaoAcessaAdministracaoGlobal() {
        autenticar(Perfil.GESTOR, tenantA);

        assertThrows(AuthorizationException.class, () -> executar(rotaGlobal(Permissao.ESCOLA_VISUALIZAR)));
    }

    @Test
    void secretariaAcessaAlunosEMatriculasDaPropriaEscola() throws Exception {
        autenticar(Perfil.SECRETARIA, tenantA);

        assertLocalPermission(Permissao.ALUNO_CRIAR);
        assertLocalPermission(Permissao.MATRICULA_CRIAR);
    }

    @Test
    void secretariaNaoAcessaAdministracaoGlobal() {
        autenticar(Perfil.SECRETARIA, tenantA);

        assertThrows(AuthorizationException.class, () -> executar(rotaGlobal(Permissao.USUARIO_VISUALIZAR)));
    }

    @Test
    void secretariaNaoAcessaRecursoDeOutroTenant() {
        autenticar(Perfil.SECRETARIA, tenantA);

        assertThrows(NotFoundException.class,
                () -> executar(rotaLocal(Permissao.MATRICULA_VISUALIZAR, tenantB, new AtomicBoolean())));
    }

    @Test
    void professorAcessaOperacaoPedagogicaDaPropriaEscola() {
        autenticar(Perfil.PROFESSOR, tenantA);

        assertLocalPermission(Permissao.NOTA_LANCAR);
    }

    @Test
    void professorNaoAcessaAdministracaoGlobal() {
        autenticar(Perfil.PROFESSOR, tenantA);

        assertThrows(AuthorizationException.class, () -> executar(rotaGlobal(Permissao.AUDITORIA_VISUALIZAR)));
    }

    @Test
    void professorNaoAcessaRecursoDeOutroTenant() {
        autenticar(Perfil.PROFESSOR, tenantA);

        assertThrows(NotFoundException.class,
                () -> executar(rotaLocal(Permissao.NOTA_VISUALIZAR, tenantB, new AtomicBoolean())));
    }

    @Test
    void financeiroAcessaDadosFinanceirosDaPropriaEscola() {
        autenticar(Perfil.FINANCEIRO, tenantA);

        assertLocalPermission(Permissao.FINANCEIRO_GERENCIAR);
    }

    @Test
    void financeiroNaoAcessaAlunosNemAdministracaoGlobal() {
        autenticar(Perfil.FINANCEIRO, tenantA);

        assertThrows(AuthorizationException.class,
                () -> executar(rotaLocal(Permissao.ALUNO_VISUALIZAR, tenantA, new AtomicBoolean())));
        assertThrows(AuthorizationException.class, () -> executar(rotaGlobal(Permissao.ESCOLA_VISUALIZAR)));
    }

    @Test
    void financeiroNaoAcessaFinanceiroDeOutroTenant() {
        autenticar(Perfil.FINANCEIRO, tenantA);

        assertThrows(NotFoundException.class,
                () -> executar(rotaLocal(Permissao.FINANCEIRO_VISUALIZAR, tenantB, new AtomicBoolean())));
    }

    @Test
    void hasPermissionRefleteAFronteiraEntreOsPerfis() {
        assertTrue(Perfil.SUPER_ADMIN.hasPermission(Permissao.AUDITORIA_VISUALIZAR));
        assertTrue(Perfil.GESTOR.hasPermission(Permissao.ALUNO_EDITAR));
        assertTrue(Perfil.SECRETARIA.hasPermission(Permissao.MATRICULA_CRIAR));
        assertTrue(Perfil.PROFESSOR.hasPermission(Permissao.NOTA_LANCAR));
        assertTrue(Perfil.FINANCEIRO.hasPermission(Permissao.FINANCEIRO_GERENCIAR));

        assertFalse(Perfil.SECRETARIA.hasPermission(Permissao.AUDITORIA_VISUALIZAR));
        assertFalse(Perfil.PROFESSOR.hasPermission(Permissao.FINANCEIRO_VISUALIZAR));
        assertFalse(Perfil.FINANCEIRO.hasPermission(Permissao.ALUNO_VISUALIZAR));
    }

    private Handler rotaGlobal(Permissao permissao) {
        return new AuthorizationMiddleware(permissao, Perfil.SUPER_ADMIN)
                .then(ctx -> { });
    }

    private Handler rotaLocal(Permissao permissao, UUID tenantDoRecurso, AtomicBoolean executada) {
        return new AuthorizationMiddleware(permissao).then(ctx -> {
            TenantAccessGuard.assertCurrentTenant(tenantDoRecurso);
            executada.set(true);
        });
    }

    private void assertGlobalPermission(Permissao permissao) throws Exception {
        AtomicBoolean executada = new AtomicBoolean();
        Handler handler = new AuthorizationMiddleware(permissao, Perfil.SUPER_ADMIN)
                .then(ctx -> executada.set(true));
        handler.handle(context);
        assertTrue(executada.get());
    }

    private void assertLocalPermission(Permissao permissao) {
        AtomicBoolean executada = new AtomicBoolean();
        assertDoesNotThrow(() -> executar(rotaLocal(permissao, tenantA, executada)));
        assertTrue(executada.get());
    }

    private void executar(Handler handler) throws Exception {
        handler.handle(context);
    }

    private void autenticar(Perfil perfil, UUID tenantId) {
        AuthUserContext.setAuthUser(new AuthUser(UUID.randomUUID(), tenantId, tenantId, perfil, "52998224725"));
    }
}
