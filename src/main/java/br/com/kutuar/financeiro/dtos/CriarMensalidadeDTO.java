package br.com.kutuar.financeiro.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CriarMensalidadeDTO {
    private UUID idAluno;
    private BigDecimal valorOriginal;
    private LocalDate dataVencimento;

    public UUID getIdAluno() { return idAluno; }
    public void setIdAluno(UUID idAluno) { this.idAluno = idAluno; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public void setValorOriginal(BigDecimal valorOriginal) { this.valorOriginal = valorOriginal; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
}