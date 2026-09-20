package br.com.kutuar.administrativo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AtividadeRecenteDTO {
    private String tipo;
    private String descricao;
    private LocalDateTime ocorridoEm;
    private UUID referenciaId;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
    public void setOcorridoEm(LocalDateTime ocorridoEm) { this.ocorridoEm = ocorridoEm; }
    public UUID getReferenciaId() { return referenciaId; }
    public void setReferenciaId(UUID referenciaId) { this.referenciaId = referenciaId; }
}
