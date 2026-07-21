package br.com.synge.academico.repositories;

import br.com.synge.academico.models.Aluno;
import br.com.synge.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AlunoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(AlunoRepository.class);

    private Aluno map(ResultSet rs) throws SQLException {
        Aluno a = new Aluno();
        a.setId(rs.getObject("id", UUID.class));
        a.setTenantId(rs.getObject("tenant_id", UUID.class));
        a.setNome(rs.getString("nome"));
        a.setCpf(rs.getString("cpf"));
        a.setDataNascimento(rs.getObject("data_nascimento", LocalDate.class));
        a.setEmail(rs.getString("email"));
        a.setTelefone(rs.getString("telefone"));
        a.setSituacao(rs.getString("situacao"));
        a.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        a.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return a;
    }

    public Aluno criar(Aluno a) {
        String sql = "INSERT INTO aluno (id, tenant_id, nome, cpf, data_nascimento, email, telefone, situacao, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, a.getId());
            ps.setObject(2, a.getTenantId());
            ps.setString(3, a.getNome());
            ps.setString(4, a.getCpf());
            ps.setObject(5, a.getDataNascimento());
            ps.setString(6, a.getEmail());
            ps.setString(7, a.getTelefone());
            ps.setString(8, a.getSituacao());
            ps.setObject(9, a.getCriadoEm());
            ps.setObject(10, a.getAtualizadoEm());
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            logger.error("Erro ao criar aluno {}: {}", a.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar aluno.", e);
        }
    }

    public boolean existsByCpf(UUID tenantId, String cpf) {
        if (cpf == null || cpf.isBlank()) return false;
        String sql = "SELECT 1 FROM aluno WHERE tenant_id = ? AND cpf = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setString(2, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar CPF do aluno {}: {}", cpf, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar CPF.", e);
        }
    }

    public Optional<Aluno> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM aluno WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar aluno {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar aluno.", e);
        }
        return Optional.empty();
    }

    public List<Aluno> listar(UUID tenantId) {
        String sql = "SELECT * FROM aluno WHERE tenant_id = ? ORDER BY nome ASC";
        List<Aluno> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar alunos do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar alunos.", e);
        }
        return lista;
    }

    public void atualizar(Aluno a) {
        String sql = "UPDATE aluno SET nome = ?, data_nascimento = ?, email = ?, telefone = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNome());
            ps.setObject(2, a.getDataNascimento());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getTelefone());
            ps.setObject(5, a.getId());
            ps.setObject(6, a.getTenantId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhum registro atualizado.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar aluno {}: {}", a.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar aluno.", e);
        }
    }

    public void atualizarSituacao(UUID tenantId, UUID id, String novaSituacao) {
        String sql = "UPDATE aluno SET situacao = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novaSituacao);
            ps.setObject(2, id);
            ps.setObject(3, tenantId);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhum registro atualizado.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar situação do aluno {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar situação do aluno.", e);
        }
    }
}
