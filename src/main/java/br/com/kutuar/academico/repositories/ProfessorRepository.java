package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.Professor;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProfessorRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProfessorRepository.class);

    private Professor map(ResultSet rs) throws SQLException {
        Professor p = new Professor();
        p.setId(rs.getObject("id", UUID.class));
        p.setTenantId(rs.getObject("tenant_id", UUID.class));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));
        p.setEmail(rs.getString("email"));
        p.setTelefone(rs.getString("telefone"));
        p.setCargaHorariaContratual(rs.getInt("carga_horaria_contratual"));
        p.setAtivo(rs.getBoolean("ativo"));
        p.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        p.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return p;
    }

    public Professor criar(Professor p) {
        String sql = "INSERT INTO professor (id, tenant_id, nome, cpf, email, telefone, carga_horaria_contratual, ativo, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, p.getId());
            ps.setObject(2, p.getTenantId());
            ps.setString(3, p.getNome());
            ps.setString(4, p.getCpf());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getTelefone());
            ps.setInt(7, p.getCargaHorariaContratual());
            ps.setBoolean(8, p.isAtivo());
            ps.setObject(9, p.getCriadoEm());
            ps.setObject(10, p.getAtualizadoEm());
            ps.executeUpdate();
            return p;
        } catch (SQLException e) {
            logger.error("Erro ao criar professor {}: {}", p.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar professor.", e);
        }
    }

    public boolean existsByCpf(UUID tenantId, String cpf) {
        String sql = "SELECT 1 FROM professor WHERE tenant_id = ? AND cpf = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setString(2, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar CPF {}: {}", cpf, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar CPF.", e);
        }
    }

    public Optional<Professor> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM professor WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar professor {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar professor.", e);
        }
        return Optional.empty();
    }

    public List<Professor> listar(UUID tenantId) {
        String sql = "SELECT * FROM professor WHERE tenant_id = ? ORDER BY nome ASC";
        List<Professor> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar professores do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar professores.", e);
        }
        return lista;
    }

    public void atualizar(Professor p) {
        String sql = "UPDATE professor SET nome = ?, email = ?, telefone = ?, carga_horaria_contratual = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getTelefone());
            ps.setInt(4, p.getCargaHorariaContratual());
            ps.setObject(5, p.getId());
            ps.setObject(6, p.getTenantId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhum registro atualizado.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar professor {}: {}", p.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar professor.", e);
        }
    }

    public void inativar(UUID tenantId, UUID id) {
        String sql = "UPDATE professor SET ativo = FALSE, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhum registro inativado.");
        } catch (SQLException e) {
            logger.error("Erro ao inativar professor {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao inativar professor.", e);
        }
    }

    public boolean existsById(UUID tenantId, UUID idProfessor) {
        String sql = "SELECT 1 FROM professor WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idProfessor);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar professor {}: {}", idProfessor, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar professor.", e);
        }
    }

    public Optional<Integer> getCargaHorariaContratual(UUID tenantId, UUID idProfessor) {
        String sql = "SELECT carga_horaria_contratual FROM professor WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idProfessor);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter carga horária contratual do professor {}: {}", idProfessor, e.getMessage(), e);
            throw new RuntimeException("Erro ao obter carga horária do professor.", e);
        }
        return Optional.empty();
    }
}
