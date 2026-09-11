package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.DocumentoAluno;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DocumentoAlunoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(DocumentoAlunoRepository.class);

    private DocumentoAluno map(ResultSet rs) throws SQLException {
        DocumentoAluno d = new DocumentoAluno();
        d.setId(rs.getObject("id", UUID.class));
        d.setTenantId(rs.getObject("tenant_id", UUID.class));
        d.setIdAluno(rs.getObject("id_aluno", UUID.class));
        d.setTipo(rs.getString("tipo"));
        d.setReferencia(rs.getString("referencia"));
        d.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        d.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return d;
    }

    public DocumentoAluno criar(DocumentoAluno d) {
        String sql = "INSERT INTO documento_aluno (id, tenant_id, id_aluno, tipo, referencia, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, d.getId());
            ps.setObject(2, d.getTenantId());
            ps.setObject(3, d.getIdAluno());
            ps.setString(4, d.getTipo());
            ps.setString(5, d.getReferencia());
            ps.setObject(6, d.getCriadoEm());
            ps.setObject(7, d.getAtualizadoEm());
            ps.executeUpdate();
            return d;
        } catch (SQLException e) {
            logger.error("Erro ao criar documento para aluno {}: {}", d.getIdAluno(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar documento.", e);
        }
    }

    public List<DocumentoAluno> listarPorAluno(UUID tenantId, UUID idAluno) {
        String sql = "SELECT * FROM documento_aluno WHERE tenant_id = ? AND id_aluno = ? ORDER BY criado_em DESC";
        List<DocumentoAluno> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAluno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar documentos do aluno {}: {}", idAluno, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar documentos.", e);
        }
        return lista;
    }

    public void remover(UUID tenantId, UUID id) {
        String sql = "DELETE FROM documento_aluno WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            int count = ps.executeUpdate();
            if (count == 0) throw new RuntimeException("Documento não encontrado para remoção.");
        } catch (SQLException e) {
            logger.error("Erro ao remover documento {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao remover documento.", e);
        }
    }
}
