package br.com.synge.financeiro.repositories;

import br.com.synge.financeiro.models.Desconto;
import br.com.synge.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
}