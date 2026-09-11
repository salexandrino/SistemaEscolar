package br.com.kutuar.academico.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class CriarAvaliacaoDTO {
    private UUID idTurma;
    private UUID idDisciplina;
    private String nome;
    private BigDecimal peso;

    public UUID getIdTurma() { return idTurma; }
    public void setIdTurma(UUID idTurma) { this.idTurma = idTurma; }
    public UUID getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(UUID idDisciplina) { this.idDisciplina = idDisciplina; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }
}