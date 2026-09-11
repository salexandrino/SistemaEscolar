package br.com.kutuar.financeiro.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Parcela {
    private UUID id;
    private UUID tenantId;
    private UUID idMensalidade;
    private int numeroParcela;
    private BigDecimal valorParcela;
    private LocalDate dataVencimento;
    private String status; // PENDENTE, PAGA, EM_ATRASO

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getIdMensalidade() { return idMensalidade; }
    public void setIdMensalidade(UUID idMensalidade) { this.idMensalidade = idMensalidade; }
    public int getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(int numeroParcela) { this.numeroParcela = numeroParcela; }
    public BigDecimal getValorParcela() { return valorParcela; }
    public void setValorParcela(BigDecimal valorParcela) { this.valorParcela = valorParcela; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}