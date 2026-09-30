package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            logger.error("Falha ao persistir evento de auditoria: acao={}, entidadeId={}",
                    evento.getAcao(), evento.getEntidadeId(), e);
            throw new IllegalStateException("Não foi possível persistir o evento de auditoria.", e);
        }
    }
}
