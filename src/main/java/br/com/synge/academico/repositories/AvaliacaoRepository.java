package br.com.synge.academico.repositories;

import br.com.synge.academico.models.Avaliacao;
import br.com.synge.seguranca.repositories.base.BaseDAO;
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

public class AvaliacaoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(AvaliacaoRepository.class);

    private Avaliacao map(ResultSet rs) throws SQLException {
        Avaliacao a = new Avaliacao();
        a.setId(rs.getObject("id", UUID.class));
        a.setTenantId(rs.getObject("tenant_id", UUID.class));
        a.setIdTurma(rs.getObject("id_turma", UUID.class));
        a.setIdDisciplina(rs.getObject("id_disciplina", UUID.class));
        a.setNome(rs.getString("nome"));
        a.setPeso(rs.getBigDecimal("peso"));
        a.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        a.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return a;
    }

    public Avaliacao criar(Avaliacao a) {
        String sql = "INSERT INTO avaliacao (id, tenant_id, id_turma, id_disciplina, nome, peso, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, a.getId());
            ps.setObject(2, a.getTenantId());
            ps.setObject(3, a.getIdTurma());
            ps.setObject(4, a.getIdDisciplina());
            ps.setString(5, a.getNome());
            ps.setBigDecimal(6, a.getPeso());
            ps.setObject(7, a.getCriadoEm());
            ps.setObject(8, a.getAtualizadoEm());
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            logger.error("Erro ao criar avaliacao {}: {}", a.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar avaliação.", e);
        }
    }

    public List<Avaliacao> listarPorTurmaEDisciplina(UUID tenantId, UUID idTurma, UUID idDisciplina) {
        String sql = "SELECT * FROM avaliacao WHERE tenant_id = ? AND id_turma = ? AND id_disciplina = ? ORDER BY criado_em ASC";
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idTurma);
            ps.setObject(3, idDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar avaliacoes: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar avaliações.", e);
        }
        return lista;
    }

    public Optional<Avaliacao> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM avaliacao WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar avaliacao {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar avaliação.", e);
        }
        return Optional.empty();
    }
}
