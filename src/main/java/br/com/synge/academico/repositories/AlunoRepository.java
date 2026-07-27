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

        a.setCodigoInep(rs.getString("codigo_inep"));
        a.setNomePai(rs.getString("nome_pai"));
        a.setNomeMae(rs.getString("nome_mae"));
        a.setSexo(rs.getString("sexo"));
        a.setCorRaca(rs.getString("cor_raca"));
        a.setNacionalidade(rs.getString("nacionalidade"));
        a.setUfNascimento(rs.getString("uf_nascimento"));
        a.setMunicipioNascimento(rs.getString("municipio_nascimento"));
        a.setNumeroCertidaoNascimento(rs.getString("numero_certidao_nascimento"));
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

        boolean usaTransporte = rs.getBoolean("usa_transporte_escolar");
        a.setUsaTransporteEscolar(rs.wasNull() ? null : usaTransporte);
        a.setResponsavelTransporte(rs.getString("responsavel_transporte"));

        a.setTipoCondicaoEspecial(rs.getString("tipo_condicao_especial"));
        a.setRecursosAcessibilidade(rs.getString("recursos_acessibilidade"));

        a.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        a.setAtualizadoEm(rs.getObject("atualizado_em", LocalDateTime.class));
        return a;
    }

    public Aluno criar(Aluno a) {
        String sql = "INSERT INTO aluno (" +
                "id, tenant_id, nome, cpf, data_nascimento, email, telefone, situacao, " +
                "codigo_inep, nome_pai, nome_mae, sexo, cor_raca, nacionalidade, uf_nascimento, " +
                "municipio_nascimento, numero_certidao_nascimento, nis, " +
                "cep, endereco, numero, complemento, bairro, cidade, estado, zona, localizacao_diferenciada, " +
                "usa_transporte_escolar, responsavel_transporte, " +
                "tipo_condicao_especial, recursos_acessibilidade, " +
                "criado_em, atualizado_em" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setObject(i++, a.getId());
            ps.setObject(i++, a.getTenantId());
            ps.setString(i++, a.getNome());
            ps.setString(i++, a.getCpf());
            ps.setObject(i++, a.getDataNascimento());
            ps.setString(i++, a.getEmail());
            ps.setString(i++, a.getTelefone());
            ps.setString(i++, a.getSituacao());

            ps.setString(i++, a.getCodigoInep());
            ps.setString(i++, a.getNomePai());
            ps.setString(i++, a.getNomeMae());
            ps.setString(i++, a.getSexo());
            ps.setString(i++, a.getCorRaca());
            ps.setString(i++, a.getNacionalidade());
            ps.setString(i++, a.getUfNascimento());
            ps.setString(i++, a.getMunicipioNascimento());
            ps.setString(i++, a.getNumeroCertidaoNascimento());
            ps.setString(i++, a.getNis());

            ps.setString(i++, a.getCep());
            ps.setString(i++, a.getEndereco());
            ps.setString(i++, a.getNumero());
            ps.setString(i++, a.getComplemento());
            ps.setString(i++, a.getBairro());
            ps.setString(i++, a.getCidade());
            ps.setString(i++, a.getEstado());
            ps.setString(i++, a.getZona());
            ps.setString(i++, a.getLocalizacaoDiferenciada());

            if (a.getUsaTransporteEscolar() != null) ps.setBoolean(i++, a.getUsaTransporteEscolar());
            else ps.setNull(i++, Types.BOOLEAN);
            ps.setString(i++, a.getResponsavelTransporte());

            ps.setString(i++, a.getTipoCondicaoEspecial());
            ps.setString(i++, a.getRecursosAcessibilidade());

            ps.setObject(i++, a.getCriadoEm());
            ps.setObject(i++, a.getAtualizadoEm());

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
        String sql = "UPDATE aluno SET " +
                "nome = ?, data_nascimento = ?, email = ?, telefone = ?, " +
                "codigo_inep = ?, nome_pai = ?, nome_mae = ?, sexo = ?, cor_raca = ?, nacionalidade = ?, " +
                "uf_nascimento = ?, municipio_nascimento = ?, numero_certidao_nascimento = ?, nis = ?, " +
                "cep = ?, endereco = ?, numero = ?, complemento = ?, bairro = ?, cidade = ?, estado = ?, " +
                "zona = ?, localizacao_diferenciada = ?, " +
                "usa_transporte_escolar = ?, responsavel_transporte = ?, " +
                "tipo_condicao_especial = ?, recursos_acessibilidade = ?, " +
                "atualizado_em = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND tenant_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, a.getNome());
            ps.setObject(i++, a.getDataNascimento());
            ps.setString(i++, a.getEmail());
            ps.setString(i++, a.getTelefone());

            ps.setString(i++, a.getCodigoInep());
            ps.setString(i++, a.getNomePai());
            ps.setString(i++, a.getNomeMae());
            ps.setString(i++, a.getSexo());
            ps.setString(i++, a.getCorRaca());
            ps.setString(i++, a.getNacionalidade());
            ps.setString(i++, a.getUfNascimento());
            ps.setString(i++, a.getMunicipioNascimento());
            ps.setString(i++, a.getNumeroCertidaoNascimento());
            ps.setString(i++, a.getNis());

            ps.setString(i++, a.getCep());
            ps.setString(i++, a.getEndereco());
            ps.setString(i++, a.getNumero());
            ps.setString(i++, a.getComplemento());
            ps.setString(i++, a.getBairro());
            ps.setString(i++, a.getCidade());
            ps.setString(i++, a.getEstado());
            ps.setString(i++, a.getZona());
            ps.setString(i++, a.getLocalizacaoDiferenciada());

            if (a.getUsaTransporteEscolar() != null) ps.setBoolean(i++, a.getUsaTransporteEscolar());
            else ps.setNull(i++, Types.BOOLEAN);
            ps.setString(i++, a.getResponsavelTransporte());

            ps.setString(i++, a.getTipoCondicaoEspecial());
            ps.setString(i++, a.getRecursosAcessibilidade());

            ps.setObject(i++, a.getId());
            ps.setObject(i++, a.getTenantId());

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