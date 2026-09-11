package br.com.kutuar.academico.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class AlunoRecuperacaoDTO {
    private UUID idAluno;
    private String nome;
    private BigDecimal mediaAtual;
    private BigDecimal notaNecessaria;

    public AlunoRecuperacaoDTO(UUID idAluno, String nome, BigDecimal mediaAtual, BigDecimal notaNecessaria) {
        this.idAluno = idAluno;
        this.nome = nome;
        this.mediaAtual = mediaAtual;
        this.notaNecessaria = notaNecessaria;
    }

    public UUID getIdAluno() { return idAluno; }
    public String getNome() { return nome; }
    public BigDecimal getMediaAtual() { return mediaAtual; }
    public BigDecimal getNotaNecessaria() { return notaNecessaria; }
}