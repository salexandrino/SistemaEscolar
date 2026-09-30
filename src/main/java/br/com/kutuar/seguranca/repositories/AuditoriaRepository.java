package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import br.com.kutuar.seguranca.enums.Perfil;

/** Persistência exclusiva para a trilha de auditoria de negócio. */
public class AuditoriaRepository extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaRepository.class);

    public List<EventoAuditoria> findAll() {
        String sql = "SELECT id, executor_id, executor_perfil, tenant_id, acao, entidade, entidade_id, detalhes, criado_em "
                + "FROM auditoria ORDER BY criado_em DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<EventoAuditoria> eventos = new ArrayList<>();
            while (result.next()) {
                EventoAuditoria evento = new EventoAuditoria();
                evento.setId(result.getObject("id", UUID.class));
                evento.setExecutorId(result.getObject("executor_id", UUID.class));
                String perfil = result.getString("executor_perfil");
                evento.setExecutorPerfil(perfil == null ? null : Perfil.valueOf(perfil));
                evento.setTenantId(result.getObject("tenant_id", UUID.class));
                evento.setAcao(result.getString("acao"));
                evento.setEntidade(result.getString("entidade"));
                evento.setEntidadeId(result.getObject("entidade_id", UUID.class));
                evento.setDetalhes(result.getString("detalhes"));
                evento.setCriadoEm(result.getObject("criado_em", LocalDateTime.class));
                eventos.add(evento);
            }
            return eventos;
        } catch (SQLException e) {
            logger.error("Falha ao listar eventos de auditoria.", e);
            throw new IllegalStateException("Não foi possível listar a auditoria.", e);
        }
    }

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
}
