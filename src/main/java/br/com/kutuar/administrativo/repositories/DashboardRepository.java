package br.com.kutuar.administrativo.repositories;

import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DashboardRepository extends BaseDAO {

    public DashboardRepository() {
    }

    public record ContagemEscolas(long total, long ativas, long inativas) {}

    public ContagemEscolas countEscolas() {
        String sql = """
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE status = 'ATIVA') AS ativas,
                       COUNT(*) FILTER (WHERE status = 'INATIVA') AS inativas
                FROM escola
                """;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new ContagemEscolas(rs.getLong("total"), rs.getLong("ativas"), rs.getLong("inativas"));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar escolas por status.", e);
        }
    }

    public long countUsuariosPendentes() {

        String sql = """
                SELECT COUNT(*)
                FROM usuario
                WHERE ativo IS NOT TRUE
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar usuários pendentes.", e);
        }
    }

    public List<Escola> findUltimasEscolas() {

        String sql = """
                SELECT id, nome, COALESCE(NULLIF(TRIM(cidade), ''), 'Não informado') AS cidade,
                       status, criado_em
                FROM escola
                ORDER BY criado_em DESC NULLS LAST, id DESC
                LIMIT 5
                """;

        List<Escola> escolas = new ArrayList<>();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Escola escola = new Escola();

                escola.setId(UUID.fromString(rs.getString("id")));
                escola.setNome(rs.getString("nome"));
                escola.setCidade(rs.getString("cidade"));
                escola.setStatus(rs.getString("status"));
                Timestamp criadoEm = rs.getTimestamp("criado_em");
                escola.setCriadoEm(criadoEm == null ? null : criadoEm.toLocalDateTime());

                escolas.add(escola);
            }

            return escolas;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar últimas escolas.", e);
        }
    }

    public List<Usuario> findUltimosUsuarios() {

        String sql = """
                SELECT id, COALESCE(nome_completo, 'Não informado') AS nome_completo, perfil, criado_em
                FROM usuario
                ORDER BY criado_em DESC NULLS LAST, id DESC
                LIMIT 5
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Usuario usuario = new Usuario();

                usuario.setId(UUID.fromString(rs.getString("id")));
                usuario.setNomeCompleto(rs.getString("nome_completo"));
                String perfil = rs.getString("perfil");
                if (perfil != null) usuario.setPerfil(Perfil.valueOf(perfil));
                Timestamp criadoEm = rs.getTimestamp("criado_em");
                usuario.setCriadoEm(criadoEm == null ? null : criadoEm.toLocalDateTime());

                usuarios.add(usuario);
            }

            return usuarios;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar últimos usuários.", e);
        }
    }

    public List<Long> findCrescimentoMensal(String tabela, LocalDate mesAtual) {
        if (!"escola".equals(tabela) && !"usuario".equals(tabela)) {
            throw new IllegalArgumentException("Tabela inválida para o dashboard.");
        }
        String sql = """
                WITH meses AS (
                    SELECT generate_series(CAST(? AS timestamp), CAST(? AS timestamp),
                                           INTERVAL '1 month') AS mes
                ), cadastros AS (
                    -- O historico anterior entra no primeiro mes da janela.
                    SELECT GREATEST(DATE_TRUNC('month', criado_em), CAST(? AS timestamp)) AS mes,
                           COUNT(*) AS quantidade
                    FROM %s
                    WHERE criado_em IS NOT NULL AND criado_em < CAST(? AS timestamp)
                    GROUP BY 1
                )
                SELECT SUM(COALESCE(c.quantidade, 0)) OVER (ORDER BY m.mes)
                FROM meses m
                LEFT JOIN cadastros c ON c.mes = m.mes
                ORDER BY m.mes
                """.formatted(tabela);
        LocalDate inicio = mesAtual.withDayOfMonth(1).minusMonths(5);
        List<Long> totais = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, inicio);
            ps.setObject(2, mesAtual.withDayOfMonth(1));
            ps.setObject(3, inicio);
            ps.setObject(4, mesAtual.withDayOfMonth(1).plusMonths(1));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) totais.add(rs.getLong(1));
            }
            return totais;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar crescimento mensal de " + tabela + ".", e);
        }
    }

    public Map<String, Long> countUsuariosPorPerfil() {
        String sql = "SELECT COALESCE(perfil, 'NAO_INFORMADO') AS perfil, COUNT(*) FROM usuario GROUP BY 1 ORDER BY 1";
        Map<String, Long> perfis = new LinkedHashMap<>();
        for (Perfil perfil : Perfil.values()) perfis.put(perfil.name(), 0L);
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) perfis.put(rs.getString(1), rs.getLong(2));
            return perfis;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar usuários por perfil.", e);
        }
    }
}
