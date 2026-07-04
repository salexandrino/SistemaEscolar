package br.com.synge.seguranca.repositories;

import br.com.synge.config.DatabaseConfig;
import br.com.synge.seguranca.repositories.base.BaseDAO;
import br.com.synge.seguranca.repositories.base.DAO;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioRepository extends BaseDAO implements DAO<Usuario, UUID> {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioRepository.class);
    private static final String SELECT_USUARIO_COLUMNS = "id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at";

    public Optional<Usuario> findByCpf(String cpf) {
        String sql = "SELECT id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at FROM usuario WHERE cpf = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por CPF {}: {}", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"), e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar usuário no banco de dados.", e);
        }
        return Optional.empty();
    }

    // Tenant-aware overloads (use these from Services when tenant is known)
    public Optional<Usuario> findByCpf(String cpf, UUID tenantId) {
        String sql = "SELECT id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at FROM usuario WHERE cpf = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setObject(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por CPF {} e tenant {}: {}", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"), tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar usuário no banco de dados.", e);
        }
        return Optional.empty();
    }

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at FROM usuario WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar usuário no banco de dados.", e);
        }
        return Optional.empty();
    }

    // Tenant-aware overload
    public Optional<Usuario> findByEmail(String email, UUID tenantId) {
        String sql = "SELECT id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at FROM usuario WHERE email = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setObject(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por email {} e tenant {}: {}", email, tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar usuário no banco de dados.", e);
        }
        return Optional.empty();
    }

    public Optional<Usuario> findById(UUID id) {
        String sql = "SELECT id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at FROM usuario WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar usuário no banco de dados.", e);
        }
        return Optional.empty();
    }

    // Tenant-aware overload
    public Optional<Usuario> findById(UUID id, UUID tenantId) {
        String sql = "SELECT id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at FROM usuario WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.setObject(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por ID {} e tenant {}: {}", id, tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar usuário no banco de dados.", e);
        }
        return Optional.empty();
    }

    public Optional<Usuario> findByIdAndTenantId(UUID id, UUID tenantId) {
        String sql = "SELECT id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at FROM usuario WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
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
            throw new RuntimeException("Erro ao buscar usuário no banco de dados.", e);
        }
        return Optional.empty();
    }

    public boolean existsByCpf(String cpf) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE cpf = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência de CPF {}: {}", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"), e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar usuário no banco de dados.", e);
        }
        return false;
    }

    // Tenant-aware overload
    public boolean existsByCpf(String cpf, UUID tenantId) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE cpf = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setObject(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar existência de CPF {} para tenant {}: {}", cpf.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"), tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar usuário no banco de dados.", e);
        }
        return false;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por email.", e);
            throw new RuntimeException("Erro ao verificar usuário no banco de dados.", e);
        }
        return false;
    }

    // Tenant-aware overload
    public boolean existsByEmail(String email, UUID tenantId) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setObject(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar usuário por email {} e tenant {}", email, tenantId, e);
            throw new RuntimeException("Erro ao verificar usuário no banco de dados.", e);
        }
        return false;
    }

    public void save(Usuario usuario) {
        // Backwards-compatible: delegate to tenant-aware save using tenantId from entity
        save(usuario, usuario.getTenantId());
    }

    // Tenant-aware save (preferred)
    public void save(Usuario usuario, UUID tenantId) {
        String sql = "INSERT INTO usuario (id, tenant_id, escola_id, nome_completo, email, cpf, telefone, senha_hash, perfil, ativo, bloqueado, tentativas_login, ultimo_login, criado_em, atualizado_em, reset_password_token, reset_password_expires_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            usuario.setId(UUID.randomUUID());
            usuario.setCriadoEm(LocalDateTime.now());
            usuario.setAtualizadoEm(LocalDateTime.now());
            usuario.setAtivo(false); // PENDENTE_APROVACAO
            usuario.setBloqueado(false);
            usuario.setTentativasLogin(0);

            // Ensure tenantId is set on entity
            usuario.setTenantId(tenantId);

            stmt.setObject(1, usuario.getId());
            stmt.setObject(2, usuario.getTenantId());
            stmt.setObject(3, usuario.getEscolaId());
            stmt.setString(4, usuario.getNomeCompleto());
            stmt.setString(5, usuario.getEmail());
            stmt.setString(6, usuario.getCpf());
            stmt.setString(7, usuario.getTelefone());
            stmt.setString(8, usuario.getSenhaHash());
            stmt.setString(9, usuario.getPerfil().name());
            stmt.setBoolean(10, usuario.isAtivo());
            stmt.setBoolean(11, usuario.isBloqueado());
            stmt.setInt(12, usuario.getTentativasLogin());
            stmt.setObject(13, usuario.getUltimoLogin(), Types.TIMESTAMP);
            stmt.setObject(14, usuario.getCriadoEm(), Types.TIMESTAMP);
            stmt.setObject(15, usuario.getAtualizadoEm(), Types.TIMESTAMP);
            stmt.setString(16, usuario.getResetPasswordToken());
            stmt.setObject(17, usuario.getResetPasswordExpiresAt(), Types.TIMESTAMP);
            stmt.executeUpdate();
            logger.info("Usuário salvo: {}", usuario.getId());
        } catch (SQLException e) {
            logger.error("Erro ao salvar usuário {}: {}", usuario.getCpfMascarado(), e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar usuário no banco de dados.", e);
        }
    }

    public void update(Usuario usuario) {
        String sql = "UPDATE usuario SET nome_completo = ?, email = ?, cpf = ?, telefone = ?, senha_hash = ?, perfil = ?, ativo = ?, bloqueado = ?, tentativas_login = ?, ultimo_login = ?, atualizado_em = ?, reset_password_token = ?, reset_password_expires_at = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNomeCompleto());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getCpf());
            stmt.setString(4, usuario.getTelefone());
            stmt.setString(5, usuario.getSenhaHash());
            stmt.setString(6, usuario.getPerfil().name());
            stmt.setBoolean(7, usuario.isAtivo());
            stmt.setBoolean(8, usuario.isBloqueado());
            stmt.setInt(9, usuario.getTentativasLogin());
            stmt.setObject(10, usuario.getUltimoLogin(), Types.TIMESTAMP);
            stmt.setObject(11, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setString(12, usuario.getResetPasswordToken());
            stmt.setObject(13, usuario.getResetPasswordExpiresAt(), Types.TIMESTAMP);
            stmt.setObject(14, usuario.getId());
            stmt.setObject(15, usuario.getTenantId());
            stmt.executeUpdate();
            logger.info("Usuário atualizado: {}", usuario.getId());
        } catch (SQLException e) {
            logger.error("Erro ao atualizar usuário {}: {}", usuario.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar usuário no banco de dados.", e);
        }
    }

    public void updateCadastro(Usuario usuario) {
        String sql = "UPDATE usuario SET escola_id = ?, nome_completo = ?, email = ?, cpf = ?, telefone = ?, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, usuario.getEscolaId());
            stmt.setString(2, usuario.getNomeCompleto());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getCpf());
            stmt.setString(5, usuario.getTelefone());
            stmt.setObject(6, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setObject(7, usuario.getId());
            stmt.setObject(8, usuario.getTenantId());
            stmt.executeUpdate();
            logger.info("Cadastro do usuario atualizado: {}", usuario.getId());
        } catch (SQLException e) {
            logger.error("Erro ao atualizar cadastro do usuario {}: {}", usuario.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar usuario no banco de dados.", e);
        }
    }

    public void approve(UUID id, UUID tenantId) {
        String sql = "UPDATE usuario SET ativo = TRUE, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setObject(2, id);
            stmt.setObject(3, tenantId);
            stmt.executeUpdate();
            logger.info("Usuário ID {} aprovado para tenant ID {}", id, tenantId);
        } catch (SQLException e) {
            logger.error("Erro ao aprovar usuário ID {} para tenant ID {}: {}", id, tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao aprovar usuário no banco de dados.", e);
        }
    }

    public void approve(UUID id) {
        String sql = "UPDATE usuario SET ativo = TRUE, atualizado_em = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setObject(2, id);
            stmt.executeUpdate();
            logger.info("Usuario ID {} aprovado.", id);
        } catch (SQLException e) {
            logger.error("Erro ao aprovar usuario ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao aprovar usuario no banco de dados.", e);
        }
    }

    public void updateProfile(UUID id, UUID tenantId, Perfil perfil) {
        String sql = "UPDATE usuario SET perfil = ?, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, perfil.name());
            stmt.setObject(2, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setObject(3, id);
            stmt.setObject(4, tenantId);
            stmt.executeUpdate();
            logger.info("Perfil do usuario ID {} atualizado.", id);
        } catch (SQLException e) {
            logger.error("Erro ao atualizar perfil do usuario ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar perfil do usuario no banco de dados.", e);
        }
    }

    public void inactivate(UUID id, UUID tenantId) {
        String sql = "UPDATE usuario SET ativo = FALSE, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setObject(2, id);
            stmt.setObject(3, tenantId);
            stmt.executeUpdate();
            logger.info("Usuario ID {} inativado.", id);
        } catch (SQLException e) {
            logger.error("Erro ao inativar usuario ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao inativar usuario no banco de dados.", e);
        }
    }

    public void updatePassword(UUID id, UUID tenantId, String newPasswordHash) {
        String sql = "UPDATE usuario SET senha_hash = ?, reset_password_token = NULL, reset_password_expires_at = NULL, atualizado_em = ? WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setObject(2, LocalDateTime.now(), Types.TIMESTAMP);
            stmt.setObject(3, id);
            stmt.setObject(4, tenantId);
            stmt.executeUpdate();
            logger.info("Senha do usuário ID {} atualizada para tenant ID {}", id, tenantId);
        } catch (SQLException e) {
            logger.error("Erro ao atualizar senha do usuário ID {} para tenant ID {}: {}", id, tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar senha do usuário no banco de dados.", e);
        }
    }

    public List<Usuario> findAllUsuarios() {
        String sql = "SELECT " + SELECT_USUARIO_COLUMNS + " FROM usuario ORDER BY criado_em DESC";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar usuarios: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar usuarios no banco de dados.", e);
        }
        return usuarios;
    }

    public List<Usuario> findAllByTenantId(UUID tenantId) {
        String sql = "SELECT " + SELECT_USUARIO_COLUMNS + " FROM usuario WHERE tenant_id = ? ORDER BY criado_em DESC";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar usuarios do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar usuarios no banco de dados.", e);
        }
        return usuarios;
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getObject("id", UUID.class));
        usuario.setTenantId(rs.getObject("tenant_id", UUID.class));
        usuario.setEscolaId(rs.getObject("escola_id", UUID.class));
        usuario.setNomeCompleto(rs.getString("nome_completo"));
        usuario.setEmail(rs.getString("email"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setTelefone(rs.getString("telefone"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setBloqueado(rs.getBoolean("bloqueado"));
        usuario.setTentativasLogin(rs.getInt("tentativas_login"));
        usuario.setUltimoLogin(rs.getObject("ultimo_login", LocalDateTime.class));
        usuario.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        usuario.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        usuario.setResetPasswordToken(rs.getString("reset_password_token"));
        usuario.setResetPasswordExpiresAt(rs.getObject("reset_password_expires_at", LocalDateTime.class));
        return usuario;
    }
    public Optional<Usuario> findSuperAdminByEmail(String email) {

        String sql = """
        SELECT *
        FROM usuario
        WHERE email = ?
        AND perfil = 'SUPER_ADMIN'
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }

            }

        } catch (SQLException e) {
            logger.error("Erro ao buscar Super Admin por email.", e);
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public java.util.List<Usuario> findAll() {
        return findAllUsuarios();
    }
}
