package br.com.kutuar.academico.dtos;

public class AtualizarSerieDTO {
    private String nome;
    private String etapaEnsino;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEtapaEnsino() { return etapaEnsino; }
    public void setEtapaEnsino(String etapaEnsino) { this.etapaEnsino = etapaEnsino; }
}