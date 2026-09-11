package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.HistoricoSituacaoAluno;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HistoricoSituacaoAlunoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(HistoricoSituacaoAlunoRepository.class);

    public void criar(HistoricoSituacaoAluno h) {
        String sql = "INSERT INTO historico_situacao_aluno (id, tenant_id, id_aluno, situacao_anterior, situacao_nova, motivo, criado_em) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, h.getId());
            ps.setObject(2, h.getTenantId());
            ps.setObject(3, h.getIdAluno());
            ps.setString(4, h.getSituacaoAnterior());
            ps.setString(5, h.getSituacaoNova());
            ps.setString(6, h.getMotivo());
            ps.setObject(7, h.getCriadoEm());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao criar histórico para aluno {}: {}", h.getIdAluno(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar histórico do aluno.", e);
        }
    }
}
