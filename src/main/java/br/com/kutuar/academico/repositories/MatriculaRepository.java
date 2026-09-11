package br.com.kutuar.academico.repositories;

import br.com.kutuar.academico.models.Matricula;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MatriculaRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(MatriculaRepository.class);

    private Matricula map(ResultSet rs) throws SQLException {
        Matricula m = new Matricula();
        m.setId(rs.getObject("id", UUID.class));
        m.setTenantId(rs.getObject("tenant_id", UUID.class));
        m.setIdAluno(rs.getObject("id_aluno", UUID.class));
        m.setIdTurma(rs.getObject("id_turma", UUID.class));
        m.setDataMatricula(rs.getObject("data_matricula", LocalDate.class));
        m.setStatus(rs.getString("status"));
        m.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        m.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return m;
    }

    public Matricula criar(Matricula m) {
        String sql = "INSERT INTO matricula (id, tenant_id, id_aluno, id_turma, data_matricula, status, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, m.getId());
            ps.setObject(2, m.getTenantId());
            ps.setObject(3, m.getIdAluno());
            ps.setObject(4, m.getIdTurma());
            ps.setObject(5, m.getDataMatricula());
            ps.setString(6, m.getStatus());
            ps.setObject(7, m.getCriadoEm());
            ps.setObject(8, m.getAtualizadoEm());
            ps.executeUpdate();
            return m;
        } catch (SQLException e) {
            logger.error("Erro ao matricular aluno {}: {}", m.getIdAluno(), e.getMessage(), e);
            throw new RuntimeException("Erro ao matricular aluno.", e);
        }
    }

    public boolean existeAtiva(UUID tenantId, UUID idAluno, UUID idTurma) {
        String sql = "SELECT 1 FROM matricula WHERE tenant_id = ? AND id_aluno = ? AND id_turma = ? AND status = 'ATIVA'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAluno);
            ps.setObject(3, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar matricula do aluno {}: {}", idAluno, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar matrícula.", e);
        }
    }

    public Optional<Matricula> obterAtivaPorAluno(UUID tenantId, UUID idAluno) {
        String sql = "SELECT * FROM matricula WHERE tenant_id = ? AND id_aluno = ? AND status = 'ATIVA'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAluno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar matricula do aluno {}: {}", idAluno, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar matrícula do aluno.", e);
        }
        return Optional.empty();
    }

    public void atualizarStatus(UUID tenantId, UUID id, String status) {
        String sql = "UPDATE matricula SET status = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, id);
            ps.setObject(3, tenantId);
            int count = ps.executeUpdate();
            if (count == 0) throw new RuntimeException("Matrícula não encontrada para atualização.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar status da matricula {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar matrícula.", e);
        }
    }

    public List<Matricula> listarPorTurma(UUID tenantId, UUID idTurma) {
        String sql = "SELECT * FROM matricula WHERE tenant_id = ? AND id_turma = ? ORDER BY data_matricula ASC";
        List<Matricula> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar matriculas da turma {}: {}", idTurma, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar matrículas da turma.", e);
        }
        return lista;
    }

    /**
     * NOVO MÉTODO: Busca os IDs dos alunos que possuem matrícula ATIVA na turma informada.
     */
    public List<UUID> listarAlunosAtivosPorTurma(UUID tenantId, UUID idTurma) {
        String sql = "SELECT id_aluno FROM matricula WHERE tenant_id = ? AND id_turma = ? AND status = 'ATIVA'";
        List<UUID> alunos = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alunos.add(rs.getObject("id_aluno", UUID.class));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar IDs dos alunos ativos da turma {}: {}", idTurma, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar alunos ativos da turma.", e);
        }
        return alunos;
    }

    /**
     * NOVO MÉTODO: Busca o histórico completo de matrículas de um aluno
     * fazendo JOIN com a tabela de turma e ano_letivo para montar os itens do histórico.
     */
    public List<br.com.kutuar.academico.dtos.HistoricoEscolarDTO.ItemMatriculaHistorico> buscarHistoricoMatriculas(UUID tenantId, UUID idAluno) {
        String sql = "SELECT m.id_turma, t.nome AS nome_turma, al.ano AS ano_letivo, m.status AS situacao_na_turma, m.atualizado_em " +
                "FROM matricula m " +
                "JOIN turma t ON m.id_turma = t.id AND m.tenant_id = t.tenant_id " +
                "JOIN ano_letivo al ON t.id_ano_letivo = al.id AND t.tenant_id = al.tenant_id " +
                "WHERE m.tenant_id = ? AND m.id_aluno = ? " +
                "ORDER BY al.ano DESC, m.data_matricula DESC";

        List<br.com.kutuar.academico.dtos.HistoricoEscolarDTO.ItemMatriculaHistorico> historico = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setObject(2, idAluno);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID idTurma = rs.getObject("id_turma", UUID.class);
                    String nomeTurma = rs.getString("nome_turma");
                    String anoLetivo = rs.getString("ano_letivo");
                    String situacaoNaTurma = rs.getString("situacao_na_turma");
                    Timestamp atualizadoEmTs = rs.getTimestamp("atualizado_em");
                    java.time.LocalDateTime dataAlteracao = atualizadoEmTs != null ? atualizadoEmTs.toLocalDateTime() : null;

                    // Instancia o record ou classe interna do DTO conforme definido
                    historico.add(new br.com.kutuar.academico.dtos.HistoricoEscolarDTO.ItemMatriculaHistorico(
                            idTurma,
                            nomeTurma,
                            anoLetivo,
                            situacaoNaTurma,
                            dataAlteracao
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar histórico de matrículas do aluno {}: {}", idAluno, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar histórico escolar.", e);
        }

        return historico;
    }
}