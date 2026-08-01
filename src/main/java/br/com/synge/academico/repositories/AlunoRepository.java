package br.com.synge.academico.repositories;

import br.com.synge.academico.models.Aluno;
import br.com.synge.seguranca.repositories.base.BaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AlunoRepository extends BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(AlunoRepository.class);

    private Aluno map(ResultSet rs) throws SQLException {
        Aluno a = new Aluno();
        a.setId(rs.getObject("id", UUID.class));
        a.setTenantId(rs.getObject("tenant_id", UUID.class));
        a.setNome(rs.getString("nome"));
        a.setCpf(rs.getString("cpf"));
        a.setDataNascimento(rs.getObject("data_nascimento", LocalDate.class));
        a.setEmail(rs.getString("email"));
        a.setTelefone(rs.getString("telefone"));
        a.setSituacao(rs.getString("situacao"));
        a.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        a.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));

        a.setCodigoInep(rs.getString("codigo_inep"));
        a.setNomePai(rs.getString("nome_pai"));
        a.setNomeMae(rs.getString("nome_mae"));
        a.setSexo(rs.getString("sexo"));
        a.setCorRaca(rs.getString("cor_raca"));
        a.setNacionalidade(rs.getString("nacionalidade"));
        a.setUfNascimento(rs.getString("uf_nascimento"));
        a.setMunicipioNascimento(rs.getString("municipio_nascimento"));
        a.setCertidaoNascimento(rs.getString("certidao_nascimento"));
        a.setNis(rs.getString("nis"));

        a.setCep(rs.getString("cep"));
        a.setEndereco(rs.getString("endereco"));
        a.setNumero(rs.getString("numero"));
        a.setComplemento(rs.getString("complemento"));
        a.setBairro(rs.getString("bairro"));
        a.setCidade(rs.getString("cidade"));
        a.setEstado(rs.getString("estado"));
        a.setZona(rs.getString("zona"));
        a.setLocalizacaoDiferenciada(rs.getString("localizacao_diferenciada"));

        a.setUsaTransporteEscolar(rs.getBoolean("usa_transporte_escolar"));
        a.setResponsavelTransporte(rs.getString("responsavel_transporte"));

        a.setTipoCondicao(rs.getString("tipo_condicao"));
        a.setRecursosAcessibilidade(rs.getString("recursos_acessibilidade"));

        return a;
    }

    public Aluno criar(Aluno a) {
        String sql = "INSERT INTO aluno (id, tenant_id, nome, cpf, data_nascimento, email, telefone, situacao, " +
                "criado_em, atualizado_em, codigo_inep, nome_pai, nome_mae, sexo, cor_raca, nacionalidade, " +
                "uf_nascimento, municipio_nascimento, certidao_nascimento, nis, cep, endereco, numero, complemento, " +
                "bairro, cidade, estado, zona, localizacao_diferenciada, usa_transporte_escolar, " +
                "responsavel_transporte, tipo_condicao, recursos_acessibilidade) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, a.getId());
            ps.setObject(2, a.getTenantId());
            ps.setString(3, a.getNome());
            ps.setString(4, a.getCpf());
            ps.setObject(5, a.getDataNascimento());
            ps.setString(6, a.getEmail());
            ps.setString(7, a.getTelefone());
            ps.setString(8, a.getSituacao());
            ps.setObject(9, a.getCriadoEm());
            ps.setObject(10, a.getAtualizadoEm());
            ps.setString(11, a.getCodigoInep());
            ps.setString(12, a.getNomePai());
            ps.setString(13, a.getNomeMae());
            ps.setString(14, a.getSexo());
            ps.setString(15, a.getCorRaca());
            ps.setString(16, a.getNacionalidade());
            ps.setString(17, a.getUfNascimento());
            ps.setString(18, a.getMunicipioNascimento());
            ps.setString(19, a.getCertidaoNascimento());
            ps.setString(20, a.getNis());
            ps.setString(21, a.getCep());
            ps.setString(22, a.getEndereco());
            ps.setString(23, a.getNumero());
            ps.setString(24, a.getComplemento());
            ps.setString(25, a.getBairro());
            ps.setString(26, a.getCidade());
            ps.setString(27, a.getEstado());
            ps.setString(28, a.getZona());
            ps.setString(29, a.getLocalizacaoDiferenciada());
            ps.setBoolean(30, a.isUsaTransporteEscolar());
            ps.setString(31, a.getResponsavelTransporte());
            ps.setString(32, a.getTipoCondicao());
            ps.setString(33, a.getRecursosAcessibilidade());
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            logger.error("Erro ao criar aluno {}: {}", a.getNome(), e.getMessage(), e);
            throw new RuntimeException("Erro ao criar aluno.", e);
        }
    }

    public boolean existsByCpf(UUID tenantId, String cpf) {
        if (cpf == null || cpf.isBlank()) return false;
        String sql = "SELECT 1 FROM aluno WHERE tenant_id = ? AND cpf = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            ps.setString(2, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erro ao verificar CPF do aluno {}: {}", cpf, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar CPF.", e);
        }
    }

    public Optional<Aluno> buscarPorId(UUID tenantId, UUID id) {
        String sql = "SELECT * FROM aluno WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setObject(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar aluno {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar aluno.", e);
        }
        return Optional.empty();
    }

    public List<Aluno> listar(UUID tenantId) {
        String sql = "SELECT * FROM aluno WHERE tenant_id = ? ORDER BY nome ASC";
        List<Aluno> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            logger.error("Erro ao listar alunos do tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Erro ao listar alunos.", e);
        }
        return lista;
    }

    public void atualizar(Aluno a) {
        String sql = "UPDATE aluno SET nome = ?, data_nascimento = ?, email = ?, telefone = ?, " +
                "codigo_inep = ?, nome_pai = ?, nome_mae = ?, sexo = ?, cor_raca = ?, nacionalidade = ?, " +
                "uf_nascimento = ?, municipio_nascimento = ?, certidao_nascimento = ?, nis = ?, cep = ?, " +
                "endereco = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, estado = ?, zona = ?, " +
                "localizacao_diferenciada = ?, usa_transporte_escolar = ?, responsavel_transporte = ?, " +
                "tipo_condicao = ?, recursos_acessibilidade = ?, atualizado_em = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNome());
            ps.setObject(2, a.getDataNascimento());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getTelefone());
            ps.setString(5, a.getCodigoInep());
            ps.setString(6, a.getNomePai());
            ps.setString(7, a.getNomeMae());
            ps.setString(8, a.getSexo());
            ps.setString(9, a.getCorRaca());
            ps.setString(10, a.getNacionalidade());
            ps.setString(11, a.getUfNascimento());
            ps.setString(12, a.getMunicipioNascimento());
            ps.setString(13, a.getCertidaoNascimento());
            ps.setString(14, a.getNis());
            ps.setString(15, a.getCep());
            ps.setString(16, a.getEndereco());
            ps.setString(17, a.getNumero());
            ps.setString(18, a.getComplemento());
            ps.setString(19, a.getBairro());
            ps.setString(20, a.getCidade());
            ps.setString(21, a.getEstado());
            ps.setString(22, a.getZona());
            ps.setString(23, a.getLocalizacaoDiferenciada());
            ps.setBoolean(24, a.isUsaTransporteEscolar());
            ps.setString(25, a.getResponsavelTransporte());
            ps.setString(26, a.getTipoCondicao());
            ps.setString(27, a.getRecursosAcessibilidade());
            ps.setObject(28, a.getId());
            ps.setObject(29, a.getTenantId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhum registro atualizado.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar aluno {}: {}", a.getId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar aluno.", e);
        }
    }

    public void atualizarSituacao(UUID tenantId, UUID id, String novaSituacao) {
        String sql = "UPDATE aluno SET situacao = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novaSituacao);
            ps.setObject(2, id);
            ps.setObject(3, tenantId);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new RuntimeException("Nenhum registro atualizado.");
        } catch (SQLException e) {
            logger.error("Erro ao atualizar situação do aluno {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar situação do aluno.", e);
        }
    }
}