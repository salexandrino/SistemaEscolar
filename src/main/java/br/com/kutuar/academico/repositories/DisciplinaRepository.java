package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.Disciplina;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DisciplinaRepository extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(DisciplinaRepository.class);

    public Disciplina criar(UUID tenantId, String nome) {
        String sql = "INSERT INTO disciplina (id, tenant_id, nome, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?)";
        Disciplina d = new Disciplina();
        d.setId(UUID.randomUUID());
        d.setTenantId(tenantId);
        d.setNome(nome);
        d.setCriadoEm(LocalDateTime.now());
        d.setAtualizadoEm(LocalDateTime.now());
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, d.getId());
            stmt.setObject(2, d.getTenantId());
            stmt.setString(3, d.getNome());
            stmt.setObject(4, d.getCriadoEm());
            stmt.setObject(5, d.getAtualizadoEm());
            stmt.executeUpdate();
            return d;
        } catch (SQLException e) {
            logger.error("Erro ao criar disciplina '{}': {}", nome, e.getMessage(), e);
            throw new RuntimeException("Erro ao criar disciplina.", e);
        }
    }

    public void atualizar(UUID tenantId, UUID id, String novoNome) {
        String sql = "UPDATE disciplina SET nome = ?, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoNome);
            stmt.setObject(2, LocalDateTime.now());
            stmt.setObject(3, id);
            stmt.setObject(4, tenantId);
            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new RuntimeException("Disciplina não encontrada para atualização.");
            }
        } catch (SQLException e) {
            logger.error("Erro ao atualizar disciplina {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar disciplina.", e);
        }
    }

    public Optional<Disciplina> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT id, tenant_id, nome, criado_em, atualizado_em FROM disciplina WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.setObject(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar disciplina {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar disciplina.", e);
        }
        return Optional.empty();
    }

    public boolean existsByNome(UUID tenantId, String nome) {
        String sql = "SELECT COUNT(1) FROM disciplina WHERE tenant_id = ? AND UPPER(nome) = UPPER(?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, tenantId);
            stmt.setString(2, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência da disciplina '{}': {}", nome, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar disciplina.", e);
        }
        return false;
    }

    public List<Disciplina> listar(UUID tenantId) {
        String sql = "SELECT id, tenant_id, nome, criado_em, atualizado_em FROM disciplina WHERE tenant_id = ? ORDER BY nome ASC";
        List<Disciplina> lista = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar disciplinas do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar disciplinas.", e);
        }
        return lista;
    }

    public void remover(UUID tenantId, UUID id) {
        String sql = "DELETE FROM disciplina WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.setObject(2, tenantId);
            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new RuntimeException("Disciplina não encontrada para remoção.");
            }
        } catch (SQLException e) {
            logger.error("Erro ao remover disciplina {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao remover disciplina.", e);
        }
    }

    private Disciplina map(ResultSet rs) throws SQLException {
        Disciplina d = new Disciplina();
        d.setId(rs.getObject("id", UUID.class));
        d.setTenantId(rs.getObject("tenant_id", UUID.class));
        d.setNome(rs.getString("nome"));
        d.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        d.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return d;
    }
}
