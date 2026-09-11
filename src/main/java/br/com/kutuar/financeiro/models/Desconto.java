package br.com.kutuar.financeiro.models;

import java.math.BigDecimal;
import java.util.UUID;

public class Desconto {
    private UUID id;
    private UUID tenantId;
    private UUID idMensalidade;
    private String motivo;
    private BigDecimal valorDesconto;

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getIdMensalidade() { return idMensalidade; }
    public void setIdMensalidade(UUID idMensalidade) { this.idMensalidade = idMensalidade; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }
}