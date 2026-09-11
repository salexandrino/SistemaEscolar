package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.SerieDisciplina;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SerieDisciplinaRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(SerieDisciplinaRepository.class);

    private SerieDisciplina map(ResultSet rs) throws SQLException {
        SerieDisciplina sd = new SerieDisciplina();
        sd.setId(rs.getObject("id", UUID.class));
        sd.setTenantId(rs.getObject("tenant_id", UUID.class));
        sd.setIdSerie(rs.getObject("id_serie", UUID.class));
        sd.setIdDisciplina(rs.getObject("id_disciplina", UUID.class));
        sd.setCargaHorariaAnual(rs.getInt("carga_horaria_anual"));
        sd.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        sd.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return sd;
    }

    public Optional<SerieDisciplina> buscar(UUID tenantId, UUID idSerie, UUID idDisciplina) {
        String sql = "SELECT * FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idSerie);
            ps.setObject(3, idDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar serie_disciplina: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar matriz.", e);
        }
        return Optional.empty();
    }

    public SerieDisciplina salvar(UUID tenantId, UUID idSerie, UUID idDisciplina, int carga) {
        // Upsert simples: tenta update, se 0 linhas, insere
        String update = "UPDATE serie_disciplina SET carga_horaria_anual = ?, atualizado_em = CURRENT_TIMESTAMP WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ?";
        String insert = "INSERT INTO serie_disciplina (id, tenant_id, id_serie, id_disciplina, carga_horaria_anual, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            try (PreparedStatement up = conn.prepareStatement(update)) {
                up.setInt(1, carga);
                up.setObject(2, tenantId);
                up.setObject(3, idSerie);
                up.setObject(4, idDisciplina);
                int n = up.executeUpdate();
                if (n > 0) {
                    return buscar(tenantId, idSerie, idDisciplina).orElseThrow();
                }
            }
            SerieDisciplina sd = new SerieDisciplina();
            sd.setId(UUID.randomUUID());
            sd.setTenantId(tenantId);
            sd.setIdSerie(idSerie);
            sd.setIdDisciplina(idDisciplina);
            sd.setCargaHorariaAnual(carga);
            sd.setCriadoEm(LocalDateTime.now());
            sd.setAtualizadoEm(LocalDateTime.now());
            try (PreparedStatement ins = conn.prepareStatement(insert)) {
                ins.setObject(1, sd.getId());
                ins.setObject(2, tenantId);
                ins.setObject(3, idSerie);
                ins.setObject(4, idDisciplina);
                ins.setInt(5, carga);
                ins.setObject(6, sd.getCriadoEm());
                ins.setObject(7, sd.getAtualizadoEm());
                ins.executeUpdate();
            }
            return sd;
        } catch (SQLException e) {
            logger.error("Erro ao salvar serie_disciplina: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar matriz.", e);
        }
    }

    public List<SerieDisciplina> listar(UUID tenantId, UUID idSerie) {
        String sql = "SELECT * FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? ORDER BY criado_em";
        List<SerieDisciplina> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idSerie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar serie_disciplina: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar matriz.", e);
        }
        return lista;
    }

    public void remover(UUID tenantId, UUID idSerie, UUID idDisciplina) {
        String sql = "DELETE FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idSerie);
            ps.setObject(3, idDisciplina);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro ao remover serie_disciplina: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao remover matriz.", e);
        }
    }

    // Utilitários sem tenantId (id_serie já é do tenant correto nas chamadas que partem da turma)
    public boolean existeNaMatriz(UUID idSerie, UUID idDisciplina) {
        String sql = "SELECT 1 FROM serie_disciplina WHERE id_serie = ? AND id_disciplina = ? LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idSerie);
            ps.setObject(2, idDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar existencia em serie_disciplina: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar matriz.", e);
        }
    }

    public Optional<Integer> obterCargaHorariaAnual(UUID idSerie, UUID idDisciplina) {
        String sql = "SELECT carga_horaria_anual FROM serie_disciplina WHERE id_serie = ? AND id_disciplina = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idSerie);
            ps.setObject(2, idDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erro ao obter carga horaria anual de serie_disciplina: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao obter carga horaria da matriz.", e);
        }
        return Optional.empty();
    }
}
