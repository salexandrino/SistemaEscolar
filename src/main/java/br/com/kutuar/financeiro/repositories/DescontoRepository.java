package br.com.kutuar.financeiro.repositories;

import br.com.kutuar.financeiro.models.Desconto;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DescontoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(DescontoRepository.class);

    public void aplicarDesconto(Desconto d) {
        String sql = "INSERT INTO desconto (id, tenant_id, id_mensalidade, motivo, valor_desconto) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, d.getId());
            ps.setObject(2, d.getTenantId());
            ps.setObject(3, d.getIdMensalidade());
            ps.setString(4, d.getMotivo());
            ps.setBigDecimal(5, d.getValorDesconto());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao registrar desconto na mensalidade {}: {}", d.getIdMensalidade(), e.getMessage());
            throw new RuntimeException("Erro ao aplicar desconto.", e);
        }
    }

    /**
     * Soma todos os descontos já concedidos para uma mensalidade.
     * Necessário para que o pagamento leve o desconto em conta em vez de
     * cobrar sempre o valor original cheio.
     */
    public BigDecimal somarDescontosPorMensalidade(UUID tenantId, UUID idMensalidade) {
        String sql = "SELECT COALESCE(SUM(valor_desconto), 0) FROM desconto WHERE tenant_id = ? AND id_mensalidade = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idMensalidade);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao somar descontos da mensalidade {}: {}", idMensalidade, e.getMessage());
            throw new RuntimeException("Erro ao consultar descontos.", e);
        }
        return BigDecimal.ZERO;
    }
}