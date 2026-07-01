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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EscolaRepository {

    private static final Logger logger = LoggerFactory.getLogger(EscolaRepository.class);

    public List<Escola> findAllAtivas() {
        String sql = "SELECT id, nome, cnpj, status, criado_em, atualizado_em FROM escola WHERE status = 'ATIVA' ORDER BY nome ASC";
        List<Escola> escolas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    escolas.add(mapResultSetToEscola(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao listar escolas ativas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar escolas no banco de dados.", e);
        }

        return escolas;
    }

    public Optional<Escola> findById(UUID id) {
        String sql = "SELECT id, nome, cnpj, status, criado_em, atualizado_em FROM escola WHERE id = ?";

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
            throw new RuntimeException("Erro ao buscar escola no banco de dados.", e);
        }
        return Optional.empty();
    }

    public Optional<Escola> findByCnpj(String cnpj) {
        String sql = "SELECT id, nome, cnpj, status, criado_em, atualizado_em FROM escola WHERE cnpj = ?";

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
            throw new RuntimeException("Erro ao buscar escola no banco de dados.", e);
        }
        return Optional.empty();
    }

    public void save(Escola escola) {
        String sql = "INSERT INTO escola (id, nome, cnpj, status, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            escola.setId(UUID.randomUUID());
            escola.setCriadoEm(LocalDateTime.now());
            escola.setAtualizadoEm(LocalDateTime.now());

            if (escola.getStatus() == null) {
                escola.setStatus("ATIVA");
            }

            stmt.setObject(1, escola.getId());
            stmt.setString(2, escola.getNome());
            stmt.setString(3, escola.getCnpj());
            stmt.setString(4, escola.getStatus());
            stmt.setObject(5, escola.getCriadoEm());
            stmt.setObject(6, escola.getAtualizadoEm());

            stmt.executeUpdate();

            logger.info("Escola salva: {}", escola.getNome());

        } catch (SQLException e) {
            logger.error("Erro ao salvar escola {}: {}", escola.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar escola.", e);
        }
    }

    public void update(Escola escola) {
        String sql = "UPDATE escola SET nome = ?, cnpj = ?, status = ?, atualizado_em = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            escola.setAtualizadoEm(LocalDateTime.now());

            stmt.setString(1, escola.getNome());
            stmt.setString(2, escola.getCnpj());
            stmt.setString(3, escola.getStatus());
            stmt.setObject(4, escola.getAtualizadoEm());
            stmt.setObject(5, escola.getId());

            stmt.executeUpdate();

            logger.info("Escola atualizada: {}", escola.getNome());

        } catch (SQLException e) {
            logger.error("Erro ao atualizar escola {}: {}", escola.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar escola.", e);
        }
    }

    private Escola mapResultSetToEscola(ResultSet rs) throws SQLException {

        Escola escola = new Escola();

        escola.setId(rs.getObject("id", UUID.class));
        escola.setNome(rs.getString("nome"));
        escola.setCnpj(rs.getString("cnpj"));
        escola.setStatus(rs.getString("status"));
        escola.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        escola.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));

        return escola;
    }
}