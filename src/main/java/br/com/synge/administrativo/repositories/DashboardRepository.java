package br.com.synge.administrativo.repositories;

import br.com.synge.config.DatabaseConfig;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.models.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardRepository {

    public DashboardRepository() {
    }

    public long countEscolas() {

        String sql = """
                SELECT COUNT(*)
                FROM escola
                """;

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar escolas.", e);
        }
    }

    public long countEscolasAtivas() {

        String sql = """
                SELECT COUNT(*)
                FROM escola
                WHERE status = 'ATIVA'
                """;

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar escolas ativas.", e);
        }
    }

    public long countEscolasInativas() {

        String sql = """
                SELECT COUNT(*)
                FROM escola
                WHERE status = 'INATIVA'
                """;

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar escolas inativas.", e);
        }
    }

    public long countUsuarios() {

        String sql = """
                SELECT COUNT(*)
                FROM usuario
                """;

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar usuários.", e);
        }
    }

    public long countUsuariosPendentes() {

        String sql = """
                SELECT COUNT(*)
                FROM usuario
                WHERE ativo = false
                """;

        try (
                Connection connection = DatabaseConfig.getConnection();
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
                SELECT *
                FROM escola
                ORDER BY criado_em DESC
                LIMIT 5
                """;

        List<Escola> escolas = new ArrayList<>();

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Escola escola = new Escola();

                escola.setId(UUID.fromString(rs.getString("id")));
                escola.setNome(rs.getString("nome"));
                escola.setCidade(rs.getString("cidade"));
                escola.setStatus(rs.getString("status"));
                escola.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());

                escolas.add(escola);
            }

            return escolas;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar últimas escolas.", e);
        }
    }

    public List<Usuario> findUltimosUsuarios() {

        String sql = """
                SELECT *
                FROM usuario
                ORDER BY criado_em DESC
                LIMIT 5
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Usuario usuario = new Usuario();

                usuario.setId(UUID.fromString(rs.getString("id")));
                usuario.setNomeCompleto(rs.getString("nome_completo"));
                usuario.setPerfil(Perfil.valueOf(rs.getString("perfil")));

                if (rs.getObject("escola_id") != null) {
                    usuario.setEscolaId(UUID.fromString(rs.getString("escola_id")));
                }

                usuario.setAtivo(rs.getBoolean("ativo"));
                usuario.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());

                usuarios.add(usuario);
            }

            return usuarios;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar últimos usuários.", e);
        }
    }
}