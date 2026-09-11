package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.Serie;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SerieRepository extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(SerieRepository.class);

    private Serie map(ResultSet rs) throws SQLException {
        Serie s = new Serie();
        s.setId(rs.getObject("id", UUID.class));
        s.setTenantId(rs.getObject("tenant_id", UUID.class));
        s.setIdAnoLetivo(rs.getObject("id_ano_letivo", UUID.class));
        s.setNome(rs.getString("nome"));
        s.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        s.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        s.setEtapaEnsino(rs.getString("etapa_ensino"));
        return s;
    }

    public Serie criar(UUID tenantId, UUID idAnoLetivo, String nome, String etapaEnsino) {
        String sql = "INSERT INTO serie (id, tenant_id, id_ano_letivo, nome, criado_em, atualizado_em, etapa_ensino) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Serie s = new Serie();
        s.setId(UUID.randomUUID());
        s.setTenantId(tenantId);
        s.setIdAnoLetivo(idAnoLetivo);
        s.setNome(nome);
        s.setCriadoEm(LocalDateTime.now());
        s.setAtualizadoEm(LocalDateTime.now());
        s.setEtapaEnsino(etapaEnsino);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, s.getId());
            ps.setObject(2, s.getTenantId());
            ps.setObject(3, s.getIdAnoLetivo());
            ps.setString(4, s.getNome());
            ps.setObject(5, s.getCriadoEm());
            ps.setObject(6, s.getAtualizadoEm());
            ps.setString(7, s.getEtapaEnsino());
            ps.executeUpdate();
            return s;
        } catch (SQLException e) {
            logger.error("Erro ao criar série '{}': {}", nome, e.getMessage(), e);
            throw new RuntimeException("Erro ao criar série.", e);
        }
    }

    public void atualizar(UUID tenantId, UUID id, String nome, String etapaEnsino) {
        String sql = "UPDATE serie SET nome = ?, etapa_ensino = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, etapaEnsino);
            ps.setObject(3, id);
            ps.setObject(4, tenantId);
            int n = ps.executeUpdate();
            if (n == 0) throw new RuntimeException("Série não encontrada para atualização.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar série {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar série.", e);
        }
    }

    public Optional<Serie> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM serie WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar série {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar série.", e);
        }
        return Optional.empty();
    }

    public boolean existsPorNomeAno(UUID tenantId, UUID idAnoLetivo, String nome) {
        String sql = "SELECT COUNT(1) FROM serie WHERE tenant_id = ? AND id_ano_letivo = ? AND UPPER(nome) = UPPER(?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAnoLetivo);
            ps.setString(3, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar série por nome/ano: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar série.", e);
        }
        return false;
    }

    public List<Serie> listarPorAnoLetivo(UUID tenantId, UUID idAnoLetivo) {
        String sql = "SELECT * FROM serie WHERE tenant_id = ? AND id_ano_letivo = ? ORDER BY nome";
        List<Serie> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAnoLetivo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar séries do ano {}: {}", idAnoLetivo, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar séries.", e);
        }
        return lista;
    }

    public void remover(UUID tenantId, UUID id) {
        String sql = "DELETE FROM serie WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            int n = ps.executeUpdate();
            if (n == 0) throw new RuntimeException("Série não encontrada para remoção.");
        } catch (SQLException e) {
            logger.error("Erro ao remover série {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao remover série.", e);
        }
    }
}