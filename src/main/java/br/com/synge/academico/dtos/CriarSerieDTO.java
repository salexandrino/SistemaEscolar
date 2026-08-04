package br.com.synge.academico.dtos;

import java.util.UUID;

public class CriarSerieDTO {
    private UUID idAnoLetivo;
    private String nome;
    private String etapaEnsino;

    public UUID getIdAnoLetivo() { return idAnoLetivo; }
    public void setIdAnoLetivo(UUID idAnoLetivo) { this.idAnoLetivo = idAnoLetivo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEtapaEnsino() { return etapaEnsino; }
    public void setEtapaEnsino(String etapaEnsino) { this.etapaEnsino = etapaEnsino; }
}