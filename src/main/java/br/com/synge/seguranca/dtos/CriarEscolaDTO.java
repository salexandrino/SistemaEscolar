package br.com.synge.seguranca.dtos;

public class CriarEscolaDTO {
    private String nome;
    private String cnpj;
    private String emailInstitucional;
    private String telefone;
    private String endereco;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String nomeResponsavel;
    private String cpfResponsavel;
    private String telefoneResponsavel;
    private String emailResponsavel;

    public CriarEscolaDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmailInstitucional() {
        return emailInstitucional;
    }

    public void setEmailInstitucional(String emailInstitucional) {
        this.emailInstitucional = emailInstitucional;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    
    private String codigoInep;
    private String situacaoFuncionamento;
    private java.time.LocalDate dataInicioAnoLetivo;
    private java.time.LocalDate dataTerminoAnoLetivo;
    private String latitude;
    private String longitude;
    private String zona;
    private String localizacaoDiferenciada;
    private String dependenciaAdministrativa;
    private String regulamentacaoNumero;
    private java.time.LocalDate regulamentacaoData;
    private String infraAgua;
    private String infraEnergia;
    private String infraEsgoto;
    private String infraLixo;
    private int qtdComputadores;
    private boolean temInternet;
    private String tipoBandaLarga;
    private String linguaMinistrada;

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public String getCpfResponsavel() {
        return cpfResponsavel;
    }

    public void setCpfResponsavel(String cpfResponsavel) {
        this.cpfResponsavel = cpfResponsavel;
    }

    public String getTelefoneResponsavel() {
        return telefoneResponsavel;
    }

    public void setTelefoneResponsavel(String telefoneResponsavel) {
        this.telefoneResponsavel = telefoneResponsavel;
    }

    public String getEmailResponsavel() {
        return emailResponsavel;
    }

    public void setEmailResponsavel(String emailResponsavel) {
        this.emailResponsavel = emailResponsavel;
    }

    public String getCodigoInep() { return codigoInep; }
    public void setCodigoInep(String codigoInep) { this.codigoInep = codigoInep; }
    public String getSituacaoFuncionamento() { return situacaoFuncionamento; }
    public void setSituacaoFuncionamento(String situacaoFuncionamento) { this.situacaoFuncionamento = situacaoFuncionamento; }
    public java.time.LocalDate getDataInicioAnoLetivo() { return dataInicioAnoLetivo; }
    public void setDataInicioAnoLetivo(java.time.LocalDate dataInicioAnoLetivo) { this.dataInicioAnoLetivo = dataInicioAnoLetivo; }
    public java.time.LocalDate getDataTerminoAnoLetivo() { return dataTerminoAnoLetivo; }
    public void setDataTerminoAnoLetivo(java.time.LocalDate dataTerminoAnoLetivo) { this.dataTerminoAnoLetivo = dataTerminoAnoLetivo; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getLocalizacaoDiferenciada() { return localizacaoDiferenciada; }
    public void setLocalizacaoDiferenciada(String localizacaoDiferenciada) { this.localizacaoDiferenciada = localizacaoDiferenciada; }
    public String getDependenciaAdministrativa() { return dependenciaAdministrativa; }
    public void setDependenciaAdministrativa(String dependenciaAdministrativa) { this.dependenciaAdministrativa = dependenciaAdministrativa; }
    public String getRegulamentacaoNumero() { return regulamentacaoNumero; }
    public void setRegulamentacaoNumero(String regulamentacaoNumero) { this.regulamentacaoNumero = regulamentacaoNumero; }
    public java.time.LocalDate getRegulamentacaoData() { return regulamentacaoData; }
    public void setRegulamentacaoData(java.time.LocalDate regulamentacaoData) { this.regulamentacaoData = regulamentacaoData; }
    public String getInfraAgua() { return infraAgua; }
    public void setInfraAgua(String infraAgua) { this.infraAgua = infraAgua; }
    public String getInfraEnergia() { return infraEnergia; }
    public void setInfraEnergia(String infraEnergia) { this.infraEnergia = infraEnergia; }
    public String getInfraEsgoto() { return infraEsgoto; }
    public void setInfraEsgoto(String infraEsgoto) { this.infraEsgoto = infraEsgoto; }
    public String getInfraLixo() { return infraLixo; }
    public void setInfraLixo(String infraLixo) { this.infraLixo = infraLixo; }
    public int getQtdComputadores() { return qtdComputadores; }
    public void setQtdComputadores(int qtdComputadores) { this.qtdComputadores = qtdComputadores; }
    public boolean isTemInternet() { return temInternet; }
    public void setTemInternet(boolean temInternet) { this.temInternet = temInternet; }
    public String getTipoBandaLarga() { return tipoBandaLarga; }
    public void setTipoBandaLarga(String tipoBandaLarga) { this.tipoBandaLarga = tipoBandaLarga; }
    public String getLinguaMinistrada() { return linguaMinistrada; }
    public void setLinguaMinistrada(String linguaMinistrada) { this.linguaMinistrada = linguaMinistrada; }

}