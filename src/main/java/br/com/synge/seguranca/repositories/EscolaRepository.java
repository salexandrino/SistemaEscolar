package br.com.synge.seguranca.repositories;

import br.com.synge.config.DatabaseConfig;
import br.com.synge.seguranca.models.Escola;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class EscolaRepository {

    private static final Logger logger = LoggerFactory.getLogger(EscolaRepository.class);

    public Optional<Escola> findById(UUID id) {
        String sql = "SELECT id, nome, cnpj, ativo, data_cadastro FROM escolas WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEscola(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar escola por ID {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Escola> findByCnpj(String cnpj) {
        String sql = "SELECT id, nome, cnpj, ativo, data_cadastro FROM escolas WHERE cnpj = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cnpj);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEscola(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar escola por CNPJ {}: {}", cnpj, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void save(Escola escola) {
        String sql = "INSERT INTO escolas (id, nome, cnpj, ativo, data_cadastro) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            escola.setId(UUID.randomUUID());
            escola.setDataCadastro(LocalDateTime.now());
            escola.setAtivo(true);

            stmt.setObject(1, escola.getId());
            stmt.setString(2, escola.getNome());
            stmt.setString(3, escola.getCnpj());
            stmt.setBoolean(4, escola.getAtivo());
            stmt.setObject(5, escola.getDataCadastro());
            stmt.executeUpdate();
            logger.info("Escola salva: {}", escola.getNome());
        } catch (SQLException e) {
            logger.error("Erro ao salvar escola {}: {}", escola.getNome(), e.getMessage(), e);
        }
    }

    public void update(Escola escola) {
        String sql = "UPDATE escolas SET nome = ?, cnpj = ?, ativo = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, escola.getNome());
            stmt.setString(2, escola.getCnpj());
            stmt.setBoolean(3, escola.getAtivo());
            stmt.setObject(4, escola.getId());
            stmt.executeUpdate();
            logger.info("Escola atualizada: {}", escola.getNome());
        } catch (SQLException e) {
            logger.error("Erro ao atualizar escola {}: {}", escola.getNome(), e.getMessage(), e);
        }
    }

    private Escola mapResultSetToEscola(ResultSet rs) throws SQLException {
        Escola escola = new Escola();
        escola.setId(rs.getObject("id", UUID.class));
        escola.setNome(rs.getString("nome"));
        escola.setCnpj(rs.getString("cnpj"));
        escola.setAtivo(rs.getBoolean("ativo"));
        escola.setDataCadastro(rs.getObject("data_cadastro", LocalDateTime.class));
        return escola;
    }
}
