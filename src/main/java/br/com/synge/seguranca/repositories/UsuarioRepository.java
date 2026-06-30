package br.com.synge.seguranca.repositories;

import br.com.synge.config.DatabaseConfig;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UsuarioRepository {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioRepository.class);

    public Optional<Usuario> findByCpf(String cpf) {
        String sql = "SELECT id, tenant_id, nome, idade, cpf, senha, perfil, tentativas_login, bloqueado_ate, criado_em, atualizado_em FROM usuarios WHERE cpf = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por CPF {}: {}", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"), e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Usuario> findByIdAndTenantId(UUID id, UUID tenantId) {
        String sql = "SELECT id, tenant_id, nome, idade, cpf, senha, perfil, tentativas_login, bloqueado_ate, criado_em, atualizado_em FROM usuarios WHERE id = ? AND tenant_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.setObject(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por ID {} e TenantId {}: {}", id, tenantId, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void update(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, idade = ?, senha = ?, perfil = ?, tentativas_login = ?, bloqueado_ate = ?, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setObject(2, usuario.getIdade(), Types.INTEGER);
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getPerfil().name());
            stmt.setObject(5, usuario.getTentativasLogin(), Types.INTEGER);
            stmt.setObject(6, usuario.getBloqueadoAte(), Types.TIMESTAMP);
            stmt.setObject(7, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setObject(8, usuario.getId());
            stmt.setObject(9, usuario.getTenantId());
            stmt.executeUpdate();
            logger.info("Usuário atualizado: {}", usuario.getId());
        } catch (SQLException e) {
            logger.error("Erro ao atualizar usuário {}: {}", usuario.getId(), e.getMessage(), e);
        }
    }

    public void save(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id, tenant_id, nome, idade, cpf, senha, perfil, tentativas_login, bloqueado_ate, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            usuario.setId(UUID.randomUUID()); // Gerar ID para novo usuário
            usuario.setCriadoEm(LocalDateTime.now());
            usuario.setAtualizadoEm(LocalDateTime.now());
            usuario.setTentativasLogin(0); // Inicializa tentativas de login

            stmt.setObject(1, usuario.getId());
            stmt.setObject(2, usuario.getTenantId());
            stmt.setString(3, usuario.getNome());
            stmt.setObject(4, usuario.getIdade(), Types.INTEGER);
            stmt.setString(5, usuario.getCpf());
            stmt.setString(6, usuario.getSenha());
            stmt.setString(7, usuario.getPerfil().name());
            stmt.setObject(8, usuario.getTentativasLogin(), Types.INTEGER);
            stmt.setObject(9, usuario.getBloqueadoAte(), Types.TIMESTAMP);
            stmt.setObject(10, usuario.getCriadoEm(), Types.TIMESTAMP);
            stmt.setObject(11, usuario.getAtualizadoEm(), Types.TIMESTAMP);
            stmt.executeUpdate();
            logger.info("Usuário salvo: {}", usuario.getId());
        } catch (SQLException e) {
            logger.error("Erro ao salvar usuário {}: {}", usuario.getCpfMascarado(), e.getMessage(), e);
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getObject("id", UUID.class));
        usuario.setTenantId(rs.getObject("tenant_id", UUID.class));
        usuario.setNome(rs.getString("nome"));
        usuario.setIdade(rs.getObject("idade", Integer.class));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        usuario.setTentativasLogin(rs.getObject("tentativas_login", Integer.class));
        usuario.setBloqueadoAte(rs.getObject("bloqueado_ate", LocalDateTime.class));
        usuario.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        usuario.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return usuario;
    }
}
