package br.com.kutuar.academico.repositories;

import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TurmaDisciplinaProfessorRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(TurmaDisciplinaProfessorRepository.class);

    public void atribuir(UUID tenantId, UUID idTurma, UUID idDisciplina, UUID idProfessor) {
        String sql = "INSERT INTO turma_disciplina_professor (id_turma, id_disciplina, id_professor, tenant_id) VALUES (?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idTurma);
            ps.setObject(2, idDisciplina);
            ps.setObject(3, idProfessor);
            ps.setObject(4, tenantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao atribuir docente {} na turma {} para disciplina {}: {}", idProfessor, idTurma, idDisciplina, e.getMessage(), e);
            throw new RuntimeException("Erro ao atribuir docente.", e);
        }
    }

    public void remover(UUID tenantId, UUID idTurma, UUID idDisciplina, UUID idProfessor) {
        String sql = "DELETE FROM turma_disciplina_professor WHERE id_turma = ? AND id_disciplina = ? AND id_professor = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idTurma);
            ps.setObject(2, idDisciplina);
            ps.setObject(3, idProfessor);
            ps.setObject(4, tenantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao remover docente {} da turma {}: {}", idProfessor, idTurma, e.getMessage(), e);
            throw new RuntimeException("Erro ao remover docente.", e);
        }
    }

    public List<UUID> listarProfessoresDaTurma(UUID tenantId, UUID idTurma) {
        String sql = "SELECT id_professor FROM turma_disciplina_professor WHERE id_turma = ? AND tenant_id = ?";
        List<UUID> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idTurma);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getObject(1, UUID.class));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar professores da turma {}: {}", idTurma, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar professores da turma.", e);
        }
        return lista;
    }

    // Soma da carga horária anual de todas as atribuições do professor no tenant, usando a matriz da série
    public int somatorioCargaHorariaProfessor(UUID tenantId, UUID idProfessor) {
        String sql = """
                SELECT COALESCE(SUM(sd.carga_horaria_anual), 0) AS total
                FROM turma_disciplina_professor tdp
                JOIN turma t ON t.id = tdp.id_turma AND t.tenant_id = tdp.tenant_id
                JOIN serie_disciplina sd ON sd.id_serie = t.id_serie AND sd.id_disciplina = tdp.id_disciplina
                WHERE tdp.tenant_id = ? AND tdp.id_professor = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            logger.error("Erro ao somar carga horária do professor {}: {}", idProfessor, e.getMessage(), e);
            throw new RuntimeException("Erro ao computar carga horária do professor.", e);
        }
        return 0;
    }

    public List<br.com.kutuar.academico.dtos.GradeProfessorItemDTO> consultarGradeProfessor(UUID tenantId, UUID idProfessor) {
        String sql = """
                SELECT t.id AS id_turma, s.nome || ' - ' || t.nome AS nome_turma, 
                       d.id AS id_disciplina, d.nome AS nome_disciplina, sd.carga_horaria_anual
                FROM turma_disciplina_professor tdp
                JOIN turma t ON t.id = tdp.id_turma AND t.tenant_id = tdp.tenant_id
                JOIN serie s ON s.id = t.id_serie AND s.tenant_id = t.tenant_id
                JOIN disciplina d ON d.id = tdp.id_disciplina AND d.tenant_id = tdp.tenant_id
                JOIN serie_disciplina sd ON sd.id_serie = t.id_serie AND sd.id_disciplina = tdp.id_disciplina
                WHERE tdp.tenant_id = ? AND tdp.id_professor = ?
                ORDER BY s.nome, t.nome, d.nome
                """;
        List<br.com.kutuar.academico.dtos.GradeProfessorItemDTO> itens = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    br.com.kutuar.academico.dtos.GradeProfessorItemDTO item = new br.com.kutuar.academico.dtos.GradeProfessorItemDTO();
                    item.setIdTurma(rs.getObject("id_turma", UUID.class));
                    item.setNomeTurma(rs.getString("nome_turma"));
                    item.setIdDisciplina(rs.getObject("id_disciplina", UUID.class));
                    item.setNomeDisciplina(rs.getString("nome_disciplina"));
                    item.setCargaHorariaAnual(rs.getInt("carga_horaria_anual"));
                    itens.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao consultar grade do professor {}: {}", idProfessor, e.getMessage(), e);
            throw new RuntimeException("Erro ao consultar grade do professor.", e);
        }
        return itens;
    }
}
