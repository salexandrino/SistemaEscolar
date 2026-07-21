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
        // CORRIGIDO: Retornando via construtor canônico do Record
        return new Avaliacao(
                rs.getObject("id", UUID.class),
                rs.getObject("tenant_id", UUID.class),
                rs.getObject("id_turma", UUID.class),
                rs.getObject("id_disciplina", UUID.class),
                rs.getString("nome"),
                rs.getBigDecimal("peso"),
                rs.getObject("criado_em", LocalDateTime.class),
                rs.getObject("atualizado_em", LocalDateTime.class)
        );
    }

    public Avaliacao criar(Avaliacao a) {
        String sql = "INSERT INTO avaliacao (id, tenant_id, id_turma, id_disciplina, nome, peso, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, a.id());
            ps.setObject(2, a.tenantId());
            ps.setObject(3, a.idTurma());
            ps.setObject(4, a.idDisciplina());
            ps.setString(5, a.nome());
            ps.setBigDecimal(6, a.peso());
            ps.setObject(7, a.criadoEm());
            ps.setObject(8, a.atualizadoEm());
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            logger.error("Erro ao criar avaliacao: {}", e.getMessage(), e);
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

    public void atualizar(Avaliacao a) {
        String sql = "UPDATE avaliacao SET nome = ?, peso = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // CORRIGIDO: Métodos do Record sem prefixo get
            ps.setString(1, a.nome());
            ps.setBigDecimal(2, a.peso());
            ps.setObject(3, a.id());
            ps.setObject(4, a.tenantId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhum registro de avaliação atualizado.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar avaliacao {}: {}", a.id(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar avaliação.", e);
        }
    }

    public void remover(UUID tenantId, UUID id) {
        String sql = "DELETE FROM avaliacao WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhuma avaliação foi encontrada para remoção.");
        } catch (SQLException e) {
            logger.error("Erro ao remover avaliacao {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao remover avaliação.", e);
        }
    }

    public boolean verificarDisciplinaNaMatrizDaTurma(UUID tenantId, UUID idTurma, UUID idDisciplina) {
        String sql = "SELECT 1 FROM turma t " +
                "JOIN serie_disciplina sd ON sd.id_serie = t.id_serie AND sd.tenant_id = t.tenant_id " +
                "WHERE t.id = ? AND sd.id_disciplina = ? AND t.tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idTurma);
            ps.setObject(2, idDisciplina);
            ps.setObject(3, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar disciplina na matriz: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao validar matriz curricular.");
        }
    }
}