package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.Turma;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TurmaRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(TurmaRepository.class);

    private Turma map(ResultSet rs) throws SQLException {
        Turma t = new Turma();
        t.setId(rs.getObject("id", UUID.class));
        t.setTenantId(rs.getObject("tenant_id", UUID.class));
        t.setIdAnoLetivo(rs.getObject("id_ano_letivo", UUID.class));
        t.setIdSerie(rs.getObject("id_serie", UUID.class));
        t.setNome(rs.getString("nome"));
        t.setTurno(rs.getString("turno"));
        t.setSala(rs.getString("sala"));
        t.setCapacidade(rs.getInt("capacidade"));
        t.setSituacao(rs.getString("situacao"));
        t.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        t.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));

        t.setTipoMediador(rs.getString("tipo_mediador"));
        t.setHoraInicio(rs.getObject("hora_inicio", LocalTime.class));
        t.setHoraTermino(rs.getObject("hora_termino", LocalTime.class));
        t.setDiasSemana(rs.getString("dias_semana"));
        int carga = rs.getInt("carga_horaria_semanal");
        t.setCargaHorariaSemanal(rs.wasNull() ? null : carga);
        t.setTipoAtendimento(rs.getString("tipo_atendimento"));
        t.setModalidadeEnsino(rs.getString("modalidade_ensino"));
        t.setFormaOrganizacao(rs.getString("forma_organizacao"));

        return t;
    }

    public Turma criar(Turma t) {
        String sql = "INSERT INTO turma (id, tenant_id, id_ano_letivo, id_serie, nome, turno, sala, capacidade, " +
                "situacao, criado_em, atualizado_em, tipo_mediador, hora_inicio, hora_termino, dias_semana, " +
                "carga_horaria_semanal, tipo_atendimento, modalidade_ensino, forma_organizacao) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, t.getId());
            ps.setObject(2, t.getTenantId());
            ps.setObject(3, t.getIdAnoLetivo());
            ps.setObject(4, t.getIdSerie());
            ps.setString(5, t.getNome());
            ps.setString(6, t.getTurno());
            ps.setString(7, t.getSala());
            ps.setInt(8, t.getCapacidade());
            ps.setString(9, t.getSituacao());
            ps.setObject(10, t.getCriadoEm());
            ps.setObject(11, t.getAtualizadoEm());
            ps.setString(12, t.getTipoMediador());
            ps.setObject(13, t.getHoraInicio());
            ps.setObject(14, t.getHoraTermino());
            ps.setString(15, t.getDiasSemana());
            ps.setObject(16, t.getCargaHorariaSemanal());
            ps.setString(17, t.getTipoAtendimento());
            ps.setString(18, t.getModalidadeEnsino());
            ps.setString(19, t.getFormaOrganizacao());
            ps.executeUpdate();
            return t;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ConflictException("Já existe uma turma com esse nome neste ano letivo.");
            }
            logger.error("Erro ao criar turma para tenant {}: {}", t.getTenantId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar turma.", e);
        }
    }

    public Optional<Turma> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM turma WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar turma {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar turma.", e);
        }
        return Optional.empty();
    }

    public List<Turma> listarTodas(UUID tenantId) {
        String sql = "SELECT * FROM turma WHERE tenant_id = ? ORDER BY nome ASC";
        List<Turma> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar todas as turmas do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar turmas.", e);
        }
        return lista;
    }


    public List<Turma> listar(UUID tenantId, UUID idAnoLetivo, UUID idSerie) {
        String sql = "SELECT * FROM turma WHERE tenant_id = ? AND id_ano_letivo = ? AND id_serie = ? ORDER BY criado_em DESC";
        List<Turma> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAnoLetivo);
            ps.setObject(3, idSerie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar turmas do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar turmas.", e);
        }
        return lista;
    }

    public void encerrar(UUID tenantId, UUID id) {
        String sql = "UPDATE turma SET situacao = 'ENCERRADA', atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            int n = ps.executeUpdate();
            if (n == 0) throw new RuntimeException("Turma não encontrada para encerrar.");
        } catch (SQLException e) {
            logger.error("Erro ao encerrar turma {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao encerrar turma.", e);
        }
    }

    public int contarMatriculas(UUID tenantId, UUID idTurma) {
        String sql = "SELECT COUNT(1) FROM matricula WHERE id_turma = ? AND tenant_id = ? AND status = 'ATIVA'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idTurma);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erro ao contar matrículas da turma {}: {}", idTurma, e.getMessage(), e);
            throw new RuntimeException("Erro ao contar matrículas.", e);
        }
        return 0;
    }

    public int contarPorSerie(UUID tenantId, UUID idSerie) {
        String sql = "SELECT COUNT(1) FROM turma WHERE id_serie = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, idSerie);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erro ao contar turmas da série {}: {}", idSerie, e.getMessage(), e);
            throw new RuntimeException("Erro ao contar turmas da série.", e);
        }
        return 0;
    }

    public void atualizar(Turma t) {
        String sql = "UPDATE turma SET nome = ?, turno = ?, sala = ?, capacidade = ?, tipo_mediador = ?, " +
                "hora_inicio = ?, hora_termino = ?, dias_semana = ?, carga_horaria_semanal = ?, " +
                "tipo_atendimento = ?, modalidade_ensino = ?, forma_organizacao = ?, atualizado_em = ? " +
                "WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getNome());
            ps.setString(2, t.getTurno());
            ps.setString(3, t.getSala());
            ps.setInt(4, t.getCapacidade());
            ps.setString(5, t.getTipoMediador());
            ps.setObject(6, t.getHoraInicio());
            ps.setObject(7, t.getHoraTermino());
            ps.setString(8, t.getDiasSemana());
            ps.setObject(9, t.getCargaHorariaSemanal());
            ps.setString(10, t.getTipoAtendimento());
            ps.setString(11, t.getModalidadeEnsino());
            ps.setString(12, t.getFormaOrganizacao());
            ps.setObject(13, t.getAtualizadoEm());
            ps.setObject(14, t.getId());
            ps.setObject(15, t.getTenantId());
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Turma não encontrada para atualização.");
            }
        } catch (SQLException e) {
            logger.error("Erro ao atualizar turma {}: {}", t.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar turma.", e);
        }
    }

    public void apagar(UUID tenantId, UUID id) {
        String sql = "DELETE FROM turma WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Turma não encontrada para exclusão.");
            }
        } catch (SQLException e) {
            logger.error("Erro ao apagar turma {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao apagar turma.", e);
        }
    }
}