package br.com.synge.academico.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AlunoResponseDTO {
    private UUID id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String email;
    private String telefone;
    private String situacao;
    private LocalDateTime criadoEm;

    private String codigoInep;
    private String nomePai;
    private String nomeMae;
    private String sexo;
    private String corRaca;
    private String nacionalidade;
    private String ufNascimento;
    private String municipioNascimento;
    private String certidaoNascimento;
    private String nis;

    private String cep;
    private String endereco;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String zona;
    private String localizacaoDiferenciada;

    private boolean usaTransporteEscolar;
    private String responsavelTransporte;

    private String tipoCondicao;
    private String recursosAcessibilidade;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public String getCodigoInep() { return codigoInep; }
    public void setCodigoInep(String codigoInep) { this.codigoInep = codigoInep; }
    public String getNomePai() { return nomePai; }
    public void setNomePai(String nomePai) { this.nomePai = nomePai; }
    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getCorRaca() { return corRaca; }
    public void setCorRaca(String corRaca) { this.corRaca = corRaca; }
    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }
    public String getUfNascimento() { return ufNascimento; }
    public void setUfNascimento(String ufNascimento) { this.ufNascimento = ufNascimento; }
    public String getMunicipioNascimento() { return municipioNascimento; }
    public void setMunicipioNascimento(String municipioNascimento) { this.municipioNascimento = municipioNascimento; }
    public String getCertidaoNascimento() { return certidaoNascimento; }
    public void setCertidaoNascimento(String certidaoNascimento) { this.certidaoNascimento = certidaoNascimento; }
    public String getNis() { return nis; }
    public void setNis(String nis) { this.nis = nis; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getLocalizacaoDiferenciada() { return localizacaoDiferenciada; }
    public void setLocalizacaoDiferenciada(String localizacaoDiferenciada) { this.localizacaoDiferenciada = localizacaoDiferenciada; }
    public boolean isUsaTransporteEscolar() { return usaTransporteEscolar; }
    public void setUsaTransporteEscolar(boolean usaTransporteEscolar) { this.usaTransporteEscolar = usaTransporteEscolar; }
    public String getResponsavelTransporte() { return responsavelTransporte; }
    public void setResponsavelTransporte(String responsavelTransporte) { this.responsavelTransporte = responsavelTransporte; }
    public String getTipoCondicao() { return tipoCondicao; }
    public void setTipoCondicao(String tipoCondicao) { this.tipoCondicao = tipoCondicao; }
    public String getRecursosAcessibilidade() { return recursosAcessibilidade; }
    public void setRecursosAcessibilidade(String recursosAcessibilidade) { this.recursosAcessibilidade = recursosAcessibilidade; }
}