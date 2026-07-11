package br.com.synge.academico.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class LancamentoNotaDTO {
    private UUID idAluno;
    private UUID idAvaliacao;
    private BigDecimal valor;

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }

    public UUID getIdAvaliacao() { return idAvaliacao; }
    public void setIdAvaliacao(UUID idAvaliacao) { this.idAvaliacao = idAvaliacao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}
