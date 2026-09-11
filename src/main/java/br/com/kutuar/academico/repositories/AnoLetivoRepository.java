package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.AnoLetivo;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class AnoLetivoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(AnoLetivoRepository.class);

    private AnoLetivo map(ResultSet rs) throws SQLException {
        AnoLetivo a = new AnoLetivo();
        a.setId(rs.getObject("id", UUID.class));
        a.setTenantId(rs.getObject("tenant_id", UUID.class));
        a.setAno(rs.getInt("ano"));
        a.setDataInicio(rs.getObject("data_inicio", LocalDate.class));
        a.setDataFim(rs.getObject("data_fim", LocalDate.class));
        a.setSituacao(rs.getString("situacao"));
        a.setAtivo(rs.getBoolean("ativo"));
        a.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        a.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return a;
    }

    public boolean existsAtivo(UUID tenantId) {
        String sql = "SELECT COUNT(1) FROM ano_letivo WHERE tenant_id = ? AND ativo = TRUE";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência de ano letivo ativo para tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar ano letivo ativo.", e);
        }
        return false;
    }

    public AnoLetivo criar(UUID tenantId, int ano, LocalDate inicio, LocalDate fim, boolean ativo) {
        String sql = "INSERT INTO ano_letivo (id, tenant_id, ano, data_inicio, data_fim, situacao, ativo, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, 'ATIVO', ?, ?, ?)";
        AnoLetivo a = new AnoLetivo();
        a.setId(UUID.randomUUID());
        a.setTenantId(tenantId);
        a.setAno(ano);
        a.setDataInicio(inicio);
        a.setDataFim(fim);
        a.setSituacao("ATIVO");
        a.setAtivo(ativo);
        a.setCriadoEm(LocalDateTime.now());
        a.setAtualizadoEm(LocalDateTime.now());
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, a.getId());
            ps.setObject(2, a.getTenantId());
            ps.setInt(3, a.getAno());
            ps.setObject(4, a.getDataInicio());
            ps.setObject(5, a.getDataFim());
            ps.setBoolean(6, a.isAtivo());
            ps.setObject(7, a.getCriadoEm());
            ps.setObject(8, a.getAtualizadoEm());
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            logger.error("Erro ao criar ano letivo {} para tenant {}: {}", ano, tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao criar ano letivo.", e);
        }
    }

    public Optional<AnoLetivo> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM ano_letivo WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar ano letivo {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar ano letivo.", e);
        }
        return Optional.empty();
    }

    public boolean existsPorAno(UUID tenantId, int ano) {
        String sql = "SELECT COUNT(1) FROM ano_letivo WHERE tenant_id = ? AND ano = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setInt(2, ano);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência de ano letivo {}: {}", ano, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar ano letivo.", e);
        }
        return false;
    }

    public void definirAtivoUnico(UUID tenantId, UUID idParaAtivar) {
        String desativar = "UPDATE ano_letivo SET ativo = FALSE, atualizado_em = CURRENT_TIMESTAMP WHERE tenant_id = ?";
        String ativar = "UPDATE ano_letivo SET ativo = TRUE, situacao = 'ATIVO', atualizado_em = CURRENT_TIMESTAMP WHERE tenant_id = ? AND id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(desativar); PreparedStatement p2 = conn.prepareStatement(ativar)) {
                p1.setObject(1, tenantId);
                p1.executeUpdate();
                p2.setObject(1, tenantId);
                p2.setObject(2, idParaAtivar);
                int count = p2.executeUpdate();
                if (count == 0) {
                    conn.rollback();
                    throw new RuntimeException("Ano letivo não encontrado para ativação.");
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Erro ao definir ano letivo ativo para tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao definir ano letivo ativo.", e);
        }
    }

    public void arquivar(UUID tenantId, UUID id) {
        // Nunca deixar um ano ARQUIVADO com ativo=TRUE
        String sql = "UPDATE ano_letivo SET situacao = 'ARQUIVADO', ativo = FALSE, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            int count = ps.executeUpdate();
            if (count == 0) throw new RuntimeException("Ano letivo não encontrado para arquivamento.");
        } catch (SQLException e) {
            logger.error("Erro ao arquivar ano letivo {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao arquivar ano letivo.", e);
        }
    }

    public List<AnoLetivo> listar(UUID tenantId) {
        String sql = "SELECT * FROM ano_letivo WHERE tenant_id = ? AND situacao = 'ATIVO' ORDER BY ano DESC";
        List<AnoLetivo> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar anos letivos do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar anos letivos.", e);
        }
        return lista;
    }

    public List<AnoLetivo> listarAnteriores(UUID tenantId) {
        String sql = "SELECT * FROM ano_letivo WHERE tenant_id = ? AND situacao = 'ARQUIVADO' ORDER BY ano DESC";
        List<AnoLetivo> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar anos anteriores do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar anos anteriores.", e);
        }
        return lista;
    }

    public void atualizar(UUID tenantId, UUID id, LocalDate inicio, LocalDate fim) {
        String sql = "UPDATE ano_letivo SET data_inicio = ?, data_fim = ?, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, inicio);
            ps.setObject(2, fim);
            ps.setObject(3, LocalDateTime.now());
            ps.setObject(4, id);
            ps.setObject(5, tenantId);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Ano letivo não encontrado para atualização.");
            }
        } catch (SQLException e) {
            logger.error("Erro ao atualizar ano letivo {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar ano letivo.", e);
        }
    }

    public int contarSeriesVinculadas(UUID tenantId, UUID idAnoLetivo) {
        String sql = "SELECT COUNT(1) FROM serie WHERE id_ano_letivo = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idAnoLetivo);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao contar séries vinculadas ao ano letivo {}: {}", idAnoLetivo, e.getMessage(), e);
            throw new RuntimeException("Erro ao contar séries vinculadas.", e);
        }
        return 0;
    }

    public void apagar(UUID tenantId, UUID id) {
        String sql = "DELETE FROM ano_letivo WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Ano letivo não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            logger.error("Erro ao apagar ano letivo {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao apagar ano letivo.", e);
        }
    }
}