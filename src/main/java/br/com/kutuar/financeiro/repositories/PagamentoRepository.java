package br.com.kutuar.financeiro.repositories;

import br.com.kutuar.financeiro.models.Pagamento;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PagamentoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(PagamentoRepository.class);

    public void registrar(Pagamento p) {
        String sql = "INSERT INTO pagamento (id, tenant_id, id_mensalidade, id_parcela, valor_pago, data_pagamento, forma_pagamento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, p.getId());
            ps.setObject(2, p.getTenantId());
            ps.setObject(3, p.getIdMensalidade());
            ps.setObject(4, p.getIdParcela()); // Pode ser nulo se pagar mensalidade cheia
            ps.setBigDecimal(5, p.getValorPago());
            ps.setTimestamp(6, Timestamp.valueOf(p.getDataPagamento()));
            ps.setString(7, p.getFormaPagamento());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao salvar transação de pagamento: {}", e.getMessage());
            throw new RuntimeException("Erro ao processar pagamento no banco de dados.", e);
        }
    }
}