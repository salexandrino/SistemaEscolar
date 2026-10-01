package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import br.com.kutuar.seguranca.dtos.EventoAuditoriaResumoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Persistência exclusiva para a trilha de auditoria de negócio. */
public class AuditoriaRepository extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaRepository.class);

    public void save(EventoAuditoria evento) {
        String sql = "INSERT INTO auditoria "
                + "(id, executor_id, executor_perfil, tenant_id, acao, entidade, entidade_id, detalhes, criado_em) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, evento.getId());
            statement.setObject(2, evento.getExecutorId());
            statement.setString(3, evento.getExecutorPerfil() == null ? null : evento.getExecutorPerfil().name());
            statement.setObject(4, evento.getTenantId());
            statement.setString(5, evento.getAcao());
            statement.setString(6, evento.getEntidade());
            statement.setObject(7, evento.getEntidadeId());
            statement.setString(8, evento.getDetalhes());
            statement.setObject(9, evento.getCriadoEm());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Falha ao persistir evento de auditoria: acao={}, entidadeId={}, tipo={}",
                    evento.getAcao(), evento.getEntidadeId(), e.getClass().getSimpleName());
            throw new IllegalStateException("Não foi possível persistir o evento de auditoria.", e);
        }
    }

    public long countFiltered(String acao, String entidade, LocalDateTime dataInicio, LocalDateTime dataFimExclusivo) {
        List<Object> parametros = new ArrayList<>();
        String where = filtroSql(acao, entidade, dataInicio, dataFimExclusivo, parametros);
        String sql = "SELECT COUNT(*) FROM auditoria a" + where;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parametros);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Falha ao contar eventos de auditoria. tipo={}", e.getClass().getSimpleName());
            throw new IllegalStateException("Não foi possível consultar a auditoria.", e);
        }
    }

    public List<EventoAuditoriaResumoDTO> findFiltered(String acao, String entidade,
                                                        LocalDateTime dataInicio, LocalDateTime dataFimExclusivo,
                                                        int size, int offset) {
        List<Object> parametros = new ArrayList<>();
        String where = filtroSql(acao, entidade, dataInicio, dataFimExclusivo, parametros);
        String sql = """
                SELECT a.id, a.criado_em,
                       CASE WHEN a.executor_id IS NULL THEN 'Sistema'
                            ELSE COALESCE(NULLIF(BTRIM(u.nome_completo), ''),
                                          NULLIF(BTRIM(u.email), ''),
                                          'Usuário não disponível')
                       END AS executor,
                       a.executor_id, a.acao, a.entidade, a.detalhes
                FROM auditoria a
                LEFT JOIN usuario u ON u.id = a.executor_id
                """ + where + " ORDER BY a.criado_em DESC, a.id DESC LIMIT ? OFFSET ?";
        parametros.add(size);
        parametros.add(offset);
        List<EventoAuditoriaResumoDTO> eventos = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parametros);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    eventos.add(new EventoAuditoriaResumoDTO(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getObject("criado_em", LocalDateTime.class),
                            resultSet.getString("executor"),
                            resultSet.getObject("executor_id", UUID.class),
                            resultSet.getString("acao"),
                            resultSet.getString("entidade"),
                            resultSet.getString("detalhes")));
                }
            }
            return eventos;
        } catch (SQLException e) {
            logger.error("Falha ao listar eventos de auditoria. tipo={}", e.getClass().getSimpleName());
            throw new IllegalStateException("Não foi possível consultar a auditoria.", e);
        }
    }

    private String filtroSql(String acao, String entidade, LocalDateTime dataInicio,
                             LocalDateTime dataFimExclusivo, List<Object> parametros) {
        List<String> condicoes = new ArrayList<>();
        adicionarFiltroTexto(condicoes, parametros, "a.acao", acao);
        adicionarFiltroTexto(condicoes, parametros, "a.entidade", entidade);
        if (dataInicio != null) {
            condicoes.add("a.criado_em >= ?");
            parametros.add(dataInicio);
        }
        if (dataFimExclusivo != null) {
            condicoes.add("a.criado_em < ?");
            parametros.add(dataFimExclusivo);
        }
        return condicoes.isEmpty() ? "" : " WHERE " + String.join(" AND ", condicoes);
    }

    private void adicionarFiltroTexto(List<String> condicoes, List<Object> parametros, String coluna, String valor) {
        if (valor == null || valor.isBlank()) return;
        condicoes.add(coluna + " ILIKE ? ESCAPE '!'");
        parametros.add("%" + valor.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
    }

    private void bind(PreparedStatement statement, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            statement.setObject(i + 1, parametros.get(i));
        }
    }
}
