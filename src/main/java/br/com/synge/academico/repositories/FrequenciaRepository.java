package br.com.synge.academico.repositories;

import br.com.synge.academico.models.Frequencia;
import br.com.synge.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FrequenciaRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(FrequenciaRepository.class);

    private Frequencia map(ResultSet rs) throws SQLException {
        Frequencia f = new Frequencia();
        f.setId(rs.getObject("id", UUID.class));
        f.setTenantId(rs.getObject("tenant_id", UUID.class));
        f.setIdTurma(rs.getObject("id_turma", UUID.class));
        f.setIdDisciplina(rs.getObject("id_disciplina", UUID.class));
        f.setIdAluno(rs.getObject("id_aluno", UUID.class));
        f.setData(rs.getObject("data", LocalDate.class));
        f.setSituacao(rs.getString("situacao"));
        f.setObservacao(rs.getString("observacao"));
        f.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        f.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return f;
    }

    public Frequencia salvar(Frequencia f) {
        String sql = "INSERT INTO frequencia (id, tenant_id, id_turma, id_disciplina, id_aluno, data, situacao, observacao, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (tenant_id, id_aluno, data, id_disciplina, id_turma) DO UPDATE SET situacao = EXCLUDED.situacao, observacao = EXCLUDED.observacao, atualizado_em = CURRENT_TIMESTAMP";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, f.getId() != null ? f.getId() : UUID.randomUUID());
            ps.setObject(2, f.getTenantId());
            ps.setObject(3, f.getIdTurma());
            ps.setObject(4, f.getIdDisciplina());
            ps.setObject(5, f.getIdAluno());
            ps.setObject(6, f.getData());
            ps.setString(7, f.getSituacao());
            ps.setString(8, f.getObservacao());
            ps.setObject(9, f.getCriadoEm() != null ? f.getCriadoEm() : LocalDateTime.now());
            ps.setObject(10, f.getAtualizadoEm() != null ? f.getAtualizadoEm() : LocalDateTime.now());
            ps.executeUpdate();
            return f;
        } catch (SQLException e) {
            logger.error("Erro ao salvar frequencia do aluno {}: {}", f.getIdAluno(), e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar frequência.", e);
        }
    }

    public List<Frequencia> listarPorAlunoEDisciplina(UUID tenantId, UUID idAluno, UUID idTurma, UUID idDisciplina) {
        String sql = "SELECT * FROM frequencia WHERE tenant_id = ? AND id_aluno = ? AND id_turma = ? AND id_disciplina = ? ORDER BY data ASC";
        List<Frequencia> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAluno);
            ps.setObject(3, idTurma);
            ps.setObject(4, idDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar frequencias: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar frequências.", e);
        }
        return lista;
    }
}
