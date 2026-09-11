package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.repositories.base.BaseDAO;
import br.com.kutuar.seguranca.repositories.base.DAO;
import br.com.kutuar.seguranca.models.RecuperacaoSenha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class RecuperacaoSenhaRepository extends BaseDAO implements DAO<RecuperacaoSenha, UUID> {

    private static final Logger logger = LoggerFactory.getLogger(RecuperacaoSenhaRepository.class);

    public RecuperacaoSenha save(RecuperacaoSenha recuperacaoSenha) {
        String sql = "INSERT INTO recuperacao_senha (id, usuario_id, codigo, expiracao, utilizado, criado_em) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            recuperacaoSenha.setId(UUID.randomUUID());
            recuperacaoSenha.setCriadoEm(LocalDateTime.now());
            recuperacaoSenha.setUtilizado(false);

            stmt.setObject(1, recuperacaoSenha.getId());
            stmt.setObject(2, recuperacaoSenha.getUsuarioId());
            stmt.setString(3, recuperacaoSenha.getCodigo());
            stmt.setObject(4, recuperacaoSenha.getExpiracao(), Types.TIMESTAMP);
            stmt.setBoolean(5, recuperacaoSenha.isUtilizado());
            stmt.setObject(6, recuperacaoSenha.getCriadoEm(), Types.TIMESTAMP);
            stmt.executeUpdate();
            logger.info("Código de recuperação salvo para usuário ID: {}", recuperacaoSenha.getUsuarioId());
        } catch (SQLException e) {
            logger.error("Erro ao salvar código de recuperação para usuário ID {}: {}", recuperacaoSenha.getUsuarioId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar código de recuperação no banco de dados.", e);
        }
        return recuperacaoSenha;
    }

    public Optional<RecuperacaoSenha> findByCodigoAndUsuarioId(String codigo, UUID usuarioId) {
        String sql = "SELECT id, usuario_id, codigo, expiracao, utilizado, criado_em FROM recuperacao_senha WHERE codigo = ? AND usuario_id = ? AND utilizado = FALSE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.setObject(2, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecuperacaoSenha(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar código de recuperação {} para usuário ID {}: {}", codigo, usuarioId, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void markAsUsed(UUID id) {
        String sql = "UPDATE recuperacao_senha SET utilizado = TRUE WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
            logger.info("Código de recuperação ID {} marcado como utilizado.", id);
        } catch (SQLException e) {
            logger.error("Erro ao marcar código de recuperação ID {} como utilizado: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao marcar código de recuperação como utilizado no banco de dados.", e);
        }
    }

    private RecuperacaoSenha mapResultSetToRecuperacaoSenha(ResultSet rs) throws SQLException {
        RecuperacaoSenha recuperacaoSenha = new RecuperacaoSenha();
        recuperacaoSenha.setId(rs.getObject("id", UUID.class));
        recuperacaoSenha.setUsuarioId(rs.getObject("usuario_id", UUID.class));
        recuperacaoSenha.setCodigo(rs.getString("codigo"));
        recuperacaoSenha.setExpiracao(rs.getObject("expiracao", LocalDateTime.class));
        recuperacaoSenha.setUtilizado(rs.getBoolean("utilizado"));
        recuperacaoSenha.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        return recuperacaoSenha;
    }

    @Override
    public void update(RecuperacaoSenha entity) {
        throw new UnsupportedOperationException("Ainda não implementado.");
    }

    @Override
    public Optional<RecuperacaoSenha> findById(UUID id) {
        throw new UnsupportedOperationException("Ainda não implementado.");
    }

    @Override
    public java.util.List<RecuperacaoSenha> findAll() {
        throw new UnsupportedOperationException("Ainda não implementado.");
    }
}
