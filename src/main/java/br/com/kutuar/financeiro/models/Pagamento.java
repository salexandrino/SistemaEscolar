package br.com.kutuar.financeiro.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Pagamento {
    private UUID id;
    private UUID tenantId;
    private UUID idMensalidade;
    private UUID idParcela; // Opcional, caso pague uma parcela avulsa
    private BigDecimal valorPago;
    private LocalDateTime dataPagamento;
    private String formaPagamento; // Ex: BOLETO, PIX, CARTAO

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getIdMensalidade() { return idMensalidade; }
    public void setIdMensalidade(UUID idMensalidade) { this.idMensalidade = idMensalidade; }
    public UUID getIdParcela() { return idParcela; }
    public void setIdParcela(UUID idParcela) { this.idParcela = idParcela; }
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
}