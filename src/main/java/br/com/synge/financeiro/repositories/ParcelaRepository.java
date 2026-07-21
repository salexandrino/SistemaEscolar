package br.com.synge.financeiro.repositories;

import br.com.synge.financeiro.models.Parcela;
import br.com.synge.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ParcelaRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(ParcelaRepository.class);

    public void salvarTodas(List<Parcela> parcelas) {
        String sql = "INSERT INTO parcela (id, tenant_id, id_mensalidade, numero_parcela, valor_parcela, data_vencimento, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); // Operação em lote (Batch)
            for (Parcela p : parcelas) {
                ps.setObject(1, p.getId());
                ps.setObject(2, p.getTenantId());
                ps.setObject(3, p.getIdMensalidade());
                ps.setInt(4, p.getNumeroParcela());
                ps.setBigDecimal(5, p.getValorParcela());
                ps.setObject(6, p.getDataVencimento());
                ps.setString(7, p.getStatus());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            logger.error("Erro ao salvar lote de parcelas: {}", e.getMessage());
            throw new RuntimeException("Erro ao salvar o parcelamento.", e);
        }
    }

    public Optional<Parcela> buscarPorId(UUID tenantId, UUID idParcela) {
        String sql = "SELECT * FROM parcela WHERE tenant_id = ? AND id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idParcela);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Parcela p = new Parcela();
                    p.setId(rs.getObject("id", UUID.class));
                    p.setTenantId(rs.getObject("tenant_id", UUID.class));
                    p.setIdMensalidade(rs.getObject("id_mensalidade", UUID.class));
                    p.setNumeroParcela(rs.getInt("numero_parcela"));
                    p.setValorParcela(rs.getBigDecimal("valor_parcela"));
                    p.setDataVencimento(rs.getObject("data_vencimento", LocalDate.class));
                    p.setStatus(rs.getString("status"));
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar parcela {}: {}", idParcela, e.getMessage());
            throw new RuntimeException("Erro ao buscar parcela.", e);
        }
        return Optional.empty();
    }

    public void atualizarStatus(UUID tenantId, UUID idParcela, String status) {
        String sql = "UPDATE parcela SET status = ? WHERE tenant_id = ? AND id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, tenantId);
            ps.setObject(3, idParcela);
            int n = ps.executeUpdate();
            if (n == 0) throw new RuntimeException("Parcela não encontrada para atualizar status.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar status da parcela {}: {}", idParcela, e.getMessage());
            throw new RuntimeException("Erro ao atualizar status da parcela.", e);
        }
    }

    public List<Parcela> listarPorMensalidade(UUID tenantId, UUID idMensalidade) {
        String sql = "SELECT * FROM parcela WHERE tenant_id = ? AND id_mensalidade = ? ORDER BY numero_parcela ASC";
        List<Parcela> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idMensalidade);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Parcela p = new Parcela();
                    p.setId(rs.getObject("id", UUID.class));
                    p.setTenantId(rs.getObject("tenant_id", UUID.class));
                    p.setIdMensalidade(rs.getObject("id_mensalidade", UUID.class));
                    p.setNumeroParcela(rs.getInt("numero_parcela"));
                    p.setValorParcela(rs.getBigDecimal("valor_parcela"));
                    p.setDataVencimento(rs.getObject("data_vencimento", LocalDate.class));
                    p.setStatus(rs.getString("status"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar parcelas da mensalidade {}: {}", idMensalidade, e.getMessage());
        }
        return lista;
    }
}