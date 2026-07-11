package br.com.synge.financeiro.repositories;

import br.com.synge.financeiro.models.Mensalidade;
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

public class MensalidadeRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(MensalidadeRepository.class);

    private Mensalidade map(ResultSet rs) throws SQLException {
        Mensalidade m = new Mensalidade();
        m.setId(rs.getObject("id", UUID.class));
        m.setTenantId(rs.getObject("tenant_id", UUID.class));
        m.setIdAluno(rs.getObject("id_aluno", UUID.class));
        m.setValorOriginal(rs.getBigDecimal("valor_original"));
        m.setDataVencimento(rs.getObject("data_vencimento", LocalDate.class));
        m.setStatus(rs.getString("status"));
        m.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        m.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return m;
    }

    public Mensalidade criar(Mensalidade m) {
        String sql = "INSERT INTO mensalidade (id, tenant_id, id_aluno, valor_original, data_vencimento, status, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, m.getId());
            ps.setObject(2, m.getTenantId());
            ps.setObject(3, m.getIdAluno());
            ps.setBigDecimal(4, m.getValorOriginal());
            ps.setObject(5, m.getDataVencimento());
            ps.setString(6, m.getStatus());
            ps.setObject(7, m.getCriadoEm());
            ps.setObject(8, m.getAtualizadoEm());
            ps.executeUpdate();
            return m;
        } catch (SQLException e) {
            logger.error("Erro ao criar mensalidade para o aluno {}: {}", m.getIdAluno(), e.getMessage());
            throw new RuntimeException("Erro ao salvar mensalidade.", e);
        }
    }

    public Optional<Mensalidade> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM mensalidade WHERE tenant_id = ? AND id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar mensalidade {}: {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    public List<Mensalidade> listarPorAluno(UUID tenantId, UUID idAluno) {
        String sql = "SELECT * FROM mensalidade WHERE tenant_id = ? AND id_aluno = ? ORDER BY data_vencimento ASC";
        List<Mensalidade> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAluno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar mensalidades do aluno {}: {}", idAluno, e.getMessage());
        }
        return lista;
    }

    public void atualizarStatus(UUID tenantId, UUID id, String status) {
        String sql = "UPDATE mensalidade SET status = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, id);
            ps.setObject(3, tenantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao atualizar status da mensalidade {}: {}", id, e.getMessage());
            throw new RuntimeException("Erro ao atualizar status financeiro.", e);
        }
    }

    // Método utilitário para listar todas em aberto ou atrasadas do tenant (Útil para o InadimplenciaService)
    public List<Mensalidade> listarPorStatus(UUID tenantId, String status) {
        String sql = "SELECT * FROM mensalidade WHERE tenant_id = ? AND status = ? ORDER BY data_vencimento ASC";
        List<Mensalidade> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar mensalidades por status: {}", e.getMessage());
        }
        return lista;
    }
}