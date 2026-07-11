package br.com.synge.financeiro.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class CriarDescontoDTO {
    private UUID idMensalidade;
    private String motivo;
    private BigDecimal valorDesconto;

    public UUID getIdMensalidade() { return idMensalidade; }
    public void setIdMensalidade(UUID idMensalidade) { this.idMensalidade = idMensalidade; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }
}