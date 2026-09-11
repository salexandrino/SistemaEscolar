package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.Nota;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NotaRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotaRepository.class);

    private Nota map(ResultSet rs) throws SQLException {
        return new Nota(
                rs.getObject("id", UUID.class),
                rs.getObject("tenant_id", UUID.class),
                rs.getObject("id_avaliacao", UUID.class),
                rs.getObject("id_aluno", UUID.class),
                rs.getBigDecimal("valor"),
                rs.getObject("criado_em", LocalDateTime.class),
                rs.getObject("atualizado_em", LocalDateTime.class)
        );
    }

    public Nota salvar(Nota n) {
        String sql = "INSERT INTO nota (id, tenant_id, id_avaliacao, id_aluno, valor, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (tenant_id, id_avaliacao, id_aluno) DO UPDATE SET valor = EXCLUDED.valor, atualizado_em = CURRENT_TIMESTAMP";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // CORREÇÃO: Acesso aos métodos do record (n.id(), n.tenantId(), etc.)
            ps.setObject(1, n.id() != null ? n.id() : UUID.randomUUID());
            ps.setObject(2, n.tenantId());
            ps.setObject(3, n.idAvaliacao());
            ps.setObject(4, n.idAluno());
            ps.setBigDecimal(5, n.valor());
            ps.setObject(6, n.criadoEm() != null ? n.criadoEm() : LocalDateTime.now());
            ps.setObject(7, n.atualizadoEm() != null ? n.atualizadoEm() : LocalDateTime.now());
            ps.executeUpdate();
            return n;
        } catch (SQLException e) {
            logger.error("Erro ao salvar nota aluno {}: {}", n.idAluno(), e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar nota.", e);
        }
    }

    public List<Nota> listarPorAvaliacoes(UUID tenantId, List<UUID> avaliacoesIds, UUID idAluno) {
        if (avaliacoesIds == null || avaliacoesIds.isEmpty()) return new ArrayList<>();
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < avaliacoesIds.size(); i++) {
            inClause.append("?");
            if (i < avaliacoesIds.size() - 1) inClause.append(",");
        }

        String sql = "SELECT * FROM nota WHERE tenant_id = ? AND id_aluno = ? AND id_avaliacao IN (" + inClause + ")";
        List<Nota> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAluno);
            int index = 3;
            for (UUID id : avaliacoesIds) {
                ps.setObject(index++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar notas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar notas.", e);
        }
        return lista;
    }
}
