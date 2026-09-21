package br.com.kutuar.administrativo;

import br.com.kutuar.administrativo.repositories.DashboardRepository;
import br.com.kutuar.administrativo.services.DashboardService;
import br.com.kutuar.seguranca.enums.Perfil;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: usa somente tabelas temporarias da conexao, sem alterar os cadastros reais. */
@EnabledIfEnvironmentVariable(named = "KUTUAR_DASHBOARD_DB_TEST", matches = "true")
class DashboardPostgresTest {
    private Connection connection;
    private DashboardRepository repository;

    @BeforeEach
    void preparar() throws Exception {
        var env = Dotenv.configure().ignoreIfMissing().load();
        connection = DriverManager.getConnection(env.get("DB_URL"), env.get("DB_USER"), env.get("DB_PASSWORD"));
        connection.setAutoCommit(false);
        executar("CREATE TEMP TABLE escola (id uuid DEFAULT gen_random_uuid(), nome text, cidade text, status text, criado_em timestamp)");
        executar("CREATE TEMP TABLE usuario (id uuid DEFAULT gen_random_uuid(), nome_completo text, perfil text, ativo boolean, criado_em timestamp)");
        // Repository fecha cada emprestimo; a conexao de teste permanece ate o rollback.
        Connection emprestimo = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    if (method.getName().equals("close")) return null;
                    try { return method.invoke(connection, args); }
                    catch (InvocationTargetException e) { throw e.getCause(); }
                });
        repository = new DashboardRepository() {
            @Override protected Connection getConnection() { return emprestimo; }
        };
    }

    @AfterEach
    void fechar() throws Exception {
        if (connection != null) { try { connection.rollback(); } finally { connection.close(); } }
    }

    private void executar(String sql) throws SQLException {
        try (var statement = connection.createStatement()) { statement.execute(sql); }
    }

    @Test
    void bancoVazioTemSeisZerosETodosOsPerfis() {
        var dto = new DashboardService(repository).buscarDashboard();
        assertEquals(0, dto.getTotalEscolas()); assertEquals(0, dto.getTotalUsuarios());
        assertEquals(0, dto.getUsuariosPendentes());
        assertEquals(List.of(0L,0L,0L,0L,0L,0L), dto.getCrescimentoEscolas());
        assertEquals(dto.getCrescimentoEscolas(), dto.getCrescimentoUsuarios());
        assertEquals(Arrays.stream(Perfil.values()).map(Enum::name).toList(), List.copyOf(dto.getUsuariosPorPerfil().keySet()));
        assertTrue(dto.getUsuariosPorPerfil().values().stream().allMatch(v -> v == 0));
        assertTrue(dto.getEscolasRecentes().isEmpty()); assertTrue(dto.getUsuariosRecentes().isEmpty());
    }

    @Test
    void acumulaHistoricoLacunasViradaDeAnoELimites() throws Exception {
        for (String tabela : List.of("escola", "usuario")) {
            executar("INSERT INTO " + tabela + " (criado_em) VALUES ('2020-01-01'), ('2025-09-30 23:59:59'),"
                    + " ('2025-10-01'), ('2025-12-31 23:59:59'), ('2026-01-01'), ('2026-03-31 23:59:59'), ('2026-04-01'), (NULL)");
            assertEquals(List.of(3L,3L,4L,5L,5L,6L), repository.findCrescimentoMensal(tabela, LocalDate.of(2026,3,1)));
        }
    }

    @Test
    void contagensPreservamStatusEPerfisSemDuplicacao() throws Exception {
        executar("INSERT INTO escola (status) VALUES ('ATIVA'), ('ATIVA'), ('INATIVA')");
        executar("INSERT INTO usuario (perfil, ativo) VALUES ('GESTOR',true), ('GESTOR',false), ('PROFESSOR',true), ('SECRETARIA',NULL)");
        var dto = new DashboardService(repository).buscarDashboard();
        assertEquals(3, dto.getTotalEscolas()); assertEquals(2, dto.getEscolasAtivas()); assertEquals(1, dto.getEscolasInativas());
        assertEquals(4, dto.getTotalUsuarios()); assertEquals(2, dto.getUsuariosPendentes());
        assertEquals(2L, dto.getUsuariosPorPerfil().get("GESTOR")); assertEquals(0L, dto.getUsuariosPorPerfil().get("FINANCEIRO"));
    }

    @Test
    void recentesTratamAusenciaDeCidadeNomePerfilEData() throws Exception {
        executar("INSERT INTO escola (nome, status) VALUES ('Escola', 'ATIVA')");
        executar("INSERT INTO usuario DEFAULT VALUES");
        var escola = repository.findUltimasEscolas().getFirst();
        var usuario = repository.findUltimosUsuarios().getFirst();
        assertEquals("Não informado", escola.getCidade()); assertNull(escola.getCriadoEm());
        assertEquals("Não informado", usuario.getNomeCompleto()); assertNull(usuario.getCriadoEm());
        assertNull(usuario.getPerfil());
        assertFalse(repository.countUsuariosPorPerfil().containsKey(null));
        // Confirma que a data ausente nao interrompe o template existente.
        var resolver = new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setCharacterEncoding("UTF-8");
        var engine = new org.thymeleaf.TemplateEngine(); engine.setTemplateResolver(resolver);
        var context = new org.thymeleaf.context.Context();
        context.setVariable("dashboard", new DashboardService(repository).buscarDashboard());
        assertDoesNotThrow(() -> engine.process("dashboard/index", context));
    }

    @Test
    void recentesLimitamCincoComOrdenacaoDeterministica() throws Exception {
        executar("INSERT INTO escola (nome, criado_em) SELECT 'Escola ' || n, timestamp '2026-01-01' + n * interval '1 day' FROM generate_series(1,7) n");
        executar("INSERT INTO usuario (nome_completo, perfil, criado_em) SELECT 'Usuario ' || n, 'GESTOR', timestamp '2026-01-01' + n * interval '1 day' FROM generate_series(1,7) n");
        executar("INSERT INTO escola (nome) VALUES ('Sem data')");
        executar("INSERT INTO usuario (nome_completo) VALUES ('Sem data')");
        var escolas = repository.findUltimasEscolas(); var usuarios = repository.findUltimosUsuarios();
        assertEquals(5, escolas.size()); assertEquals(5, usuarios.size());
        assertEquals("Escola 7", escolas.getFirst().getNome()); assertEquals("Escola 3", escolas.getLast().getNome());
        assertEquals("Usuario 7", usuarios.getFirst().getNomeCompleto()); assertEquals("Usuario 3", usuarios.getLast().getNomeCompleto());
    }
}
