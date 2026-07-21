package br.com.synge.academico.dtos;

import java.util.UUID;

public class DocumentoAlunoDTO {
    private UUID id;
    private String tipo;
    private String referencia;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
