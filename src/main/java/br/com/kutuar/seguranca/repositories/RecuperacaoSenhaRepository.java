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
import java.util.ArrayList;
import java.util.List;

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
            logger.info("Recuperação de senha registrada. usuarioId={}", recuperacaoSenha.getUsuarioId());
        } catch (SQLException e) {
            logger.error("Erro ao registrar recuperação de senha. usuarioId={}, tipo={}",
                    recuperacaoSenha.getUsuarioId(), e.getClass().getSimpleName());
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
            logger.error("Erro ao consultar recuperação de senha. usuarioId={}, tipo={}",
                    usuarioId, e.getClass().getSimpleName());
        }
        return Optional.empty();
    }

    public void markAsUsed(UUID id) {
        String sql = "UPDATE recuperacao_senha SET utilizado = TRUE WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
            logger.info("Recuperação de senha marcada como utilizada. recuperacaoId={}", id);
        } catch (SQLException e) {
            logger.error("Erro ao marcar recuperação de senha como utilizada. recuperacaoId={}, tipo={}",
                    id, e.getClass().getSimpleName());
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
        String sql = "UPDATE recuperacao_senha SET usuario_id = ?, codigo = ?, expiracao = ?, utilizado = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, entity.getUsuarioId());
            stmt.setString(2, entity.getCodigo());
            stmt.setObject(3, entity.getExpiracao(), Types.TIMESTAMP);
            stmt.setBoolean(4, entity.isUtilizado());
            stmt.setObject(5, entity.getId());
            if (stmt.executeUpdate() == 0) {
                throw new RuntimeException("Recuperação de senha não encontrada.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar recuperação de senha.", e);
        }
    }

    @Override
    public Optional<RecuperacaoSenha> findById(UUID id) {
        String sql = "SELECT id, usuario_id, codigo, expiracao, utilizado, criado_em FROM recuperacao_senha WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSetToRecuperacaoSenha(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar recuperação de senha.", e);
        }
    }

    @Override
    public List<RecuperacaoSenha> findAll() {
        String sql = "SELECT id, usuario_id, codigo, expiracao, utilizado, criado_em FROM recuperacao_senha ORDER BY criado_em DESC";
        List<RecuperacaoSenha> recuperacoes = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recuperacoes.add(mapResultSetToRecuperacaoSenha(rs));
            }
            return recuperacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar recuperações de senha.", e);
        }
    }
}
