package br.com.synge.seguranca.repositories;

import br.com.synge.config.DatabaseConfig;
import br.com.synge.seguranca.repositories.base.BaseDAO;
import br.com.synge.seguranca.repositories.base.DAO;
import br.com.synge.seguranca.models.Escola;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EscolaRepository extends BaseDAO implements DAO<Escola, UUID> {

    private static final Logger logger = LoggerFactory.getLogger(EscolaRepository.class);

    @Override
    public List<Escola> findAll() {
        String sql = "SELECT id, tenant_id, nome, cnpj, email_institucional, telefone, endereco, numero, " +
                     "complemento, bairro, cidade, estado, cep, nome_responsavel, telefone_responsavel, " +
                     "email_responsavel, status, criado_em, atualizado_em FROM escola ORDER BY nome ASC";
        List<Escola> escolas = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    escolas.add(mapResultSetToEscola(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao listar todas as escolas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar escolas no banco de dados.", e);
        }

        return escolas;
    }

    public List<Escola> findAllAtivas() {
        String sql = "SELECT id, tenant_id, nome, cnpj, email_institucional, telefone, endereco, numero, " +
                     "complemento, bairro, cidade, estado, cep, nome_responsavel, telefone_responsavel, " +
                     "email_responsavel, status, criado_em, atualizado_em FROM escola WHERE status = 'ATIVA' ORDER BY nome ASC";
        List<Escola> escolas = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    escolas.add(mapResultSetToEscola(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao listar escolas ativas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar escolas no banco de dados.", e);
        }

        return escolas;
    }

    public List<Escola> findAllInativas() {
        String sql = "SELECT id, tenant_id, nome, cnpj, email_institucional, telefone, endereco, numero, " +
                     "complemento, bairro, cidade, estado, cep, nome_responsavel, telefone_responsavel, " +
                     "email_responsavel, status, criado_em, atualizado_em FROM escola WHERE status = 'INATIVA' ORDER BY nome ASC";
        List<Escola> escolas = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    escolas.add(mapResultSetToEscola(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao listar escolas inativas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar escolas no banco de dados.", e);
        }

        return escolas;
    }

    @Override
    public Optional<Escola> findById(UUID id) {
        String sql = "SELECT id, tenant_id, nome, cnpj, email_institucional, telefone, endereco, numero, " +
                     "complemento, bairro, cidade, estado, cep, nome_responsavel, telefone_responsavel, " +
                     "email_responsavel, status, criado_em, atualizado_em FROM escola WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEscola(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao buscar escola por ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar escola no banco de dados.", e);
        }
        return Optional.empty();
    }

    public Optional<Escola> findByCnpj(String cnpj) {
        String sql = "SELECT id, tenant_id, nome, cnpj, email_institucional, telefone, endereco, numero, " +
                     "complemento, bairro, cidade, estado, cep, nome_responsavel, telefone_responsavel, " +
                     "email_responsavel, status, criado_em, atualizado_em FROM escola WHERE cnpj = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEscola(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Erro ao buscar escola por CNPJ {}: {}", cnpj, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar escola no banco de dados.", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Escola escola) {
        String sql = "INSERT INTO escola (id, tenant_id, nome, cnpj, email_institucional, telefone, endereco, " +
                     "numero, complemento, bairro, cidade, estado, cep, nome_responsavel, telefone_responsavel, " +
                     "email_responsavel, status, criado_em, atualizado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            escola.setId(UUID.randomUUID());
            escola.setTenantId(UUID.randomUUID());
            escola.setCriadoEm(LocalDateTime.now());
            escola.setAtualizadoEm(LocalDateTime.now());

            if (escola.getStatus() == null) {
                escola.setStatus("ATIVA");
            }

            stmt.setObject(1, escola.getId());
            stmt.setObject(2, escola.getTenantId());
            stmt.setString(3, escola.getNome());
            stmt.setString(4, escola.getCnpj());
            stmt.setString(5, escola.getEmailInstitucional());
            stmt.setString(6, escola.getTelefone());
            stmt.setString(7, escola.getEndereco());
            stmt.setString(8, escola.getNumero());
            stmt.setString(9, escola.getComplemento());
            stmt.setString(10, escola.getBairro());
            stmt.setString(11, escola.getCidade());
            stmt.setString(12, escola.getEstado());
            stmt.setString(13, escola.getCep());
            stmt.setString(14, escola.getNomeResponsavel());
            stmt.setString(15, escola.getTelefoneResponsavel());
            stmt.setString(16, escola.getEmailResponsavel());
            stmt.setString(17, escola.getStatus());
            stmt.setObject(18, escola.getCriadoEm());
            stmt.setObject(19, escola.getAtualizadoEm());

            stmt.executeUpdate();

            logger.info("Escola salva: {} (ID: {}, Tenant: {})", escola.getNome(), escola.getId(), escola.getTenantId());

        } catch (SQLException e) {
            logger.error("Erro ao salvar escola {}: {}", escola.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar escola.", e);
        }
    }

    @Override
    public void update(Escola escola) {
        String sql = "UPDATE escola SET nome = ?, cnpj = ?, email_institucional = ?, telefone = ?, " +
                     "endereco = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, estado = ?, cep = ?, " +
                     "nome_responsavel = ?, telefone_responsavel = ?, email_responsavel = ?, status = ?, " +
                     "atualizado_em = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            escola.setAtualizadoEm(LocalDateTime.now());

            stmt.setString(1, escola.getNome());
            stmt.setString(2, escola.getCnpj());
            stmt.setString(3, escola.getEmailInstitucional());
            stmt.setString(4, escola.getTelefone());
            stmt.setString(5, escola.getEndereco());
            stmt.setString(6, escola.getNumero());
            stmt.setString(7, escola.getComplemento());
            stmt.setString(8, escola.getBairro());
            stmt.setString(9, escola.getCidade());
            stmt.setString(10, escola.getEstado());
            stmt.setString(11, escola.getCep());
            stmt.setString(12, escola.getNomeResponsavel());
            stmt.setString(13, escola.getTelefoneResponsavel());
            stmt.setString(14, escola.getEmailResponsavel());
            stmt.setString(15, escola.getStatus());
            stmt.setObject(16, escola.getAtualizadoEm());
            stmt.setObject(17, escola.getId());

            stmt.executeUpdate();

            logger.info("Escola atualizada: {} (ID: {})", escola.getNome(), escola.getId());

        } catch (SQLException e) {
            logger.error("Erro ao atualizar escola {}: {}", escola.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar escola.", e);
        }
    }

    private Escola mapResultSetToEscola(ResultSet rs) throws SQLException {
        Escola escola = new Escola();

        escola.setId(rs.getObject("id", UUID.class));
        escola.setTenantId(rs.getObject("tenant_id", UUID.class));
        escola.setNome(rs.getString("nome"));
        escola.setCnpj(rs.getString("cnpj"));
        escola.setEmailInstitucional(rs.getString("email_institucional"));
        escola.setTelefone(rs.getString("telefone"));
        escola.setEndereco(rs.getString("endereco"));
        escola.setNumero(rs.getString("numero"));
        escola.setComplemento(rs.getString("complemento"));
        escola.setBairro(rs.getString("bairro"));
        escola.setCidade(rs.getString("cidade"));
        escola.setEstado(rs.getString("estado"));
        escola.setCep(rs.getString("cep"));
        escola.setNomeResponsavel(rs.getString("nome_responsavel"));
        escola.setTelefoneResponsavel(rs.getString("telefone_responsavel"));
        escola.setEmailResponsavel(rs.getString("email_responsavel"));
        escola.setStatus(rs.getString("status"));
        escola.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        escola.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));

        return escola;
    }
}