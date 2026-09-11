package br.com.kutuar.financeiro.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class RegistrarPagamentoDTO {
    private UUID idMensalidade;
    private UUID idParcela; // Opcional (pode vir nulo)
    private BigDecimal valorPago;
    private String formaPagamento;

    public UUID getIdMensalidade() { return idMensalidade; }
    public void setIdMensalidade(UUID idMensalidade) { this.idMensalidade = idMensalidade; }
    public UUID getIdParcela() { return idParcela; }
    public void setIdParcela(UUID idParcela) { this.idParcela = idParcela; }
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
}