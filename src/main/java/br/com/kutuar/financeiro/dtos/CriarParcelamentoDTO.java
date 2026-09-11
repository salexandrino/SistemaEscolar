package br.com.kutuar.financeiro.dtos;

import java.util.UUID;

public class CriarParcelamentoDTO {
    private UUID idMensalidade;
    private int quantidadeParcelas;

    public UUID getIdMensalidade() { return idMensalidade; }
    public void setIdMensalidade(UUID idMensalidade) { this.idMensalidade = idMensalidade; }
    public int getQuantidadeParcelas() { return quantidadeParcelas; }
    public void setQuantidadeParcelas(int quantidadeParcelas) { this.quantidadeParcelas = quantidadeParcelas; }
}