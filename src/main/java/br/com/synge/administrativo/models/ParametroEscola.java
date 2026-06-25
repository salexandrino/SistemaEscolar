package br.com.synge.administrativo.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "parametros_escola")
public class ParametroEscola {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID escolaId;
    private String chave; // Ex: TIPO_CALENDARIO, FORMATO_CALCULO_MEDIA
    private String valor; // Ex: BIMESTRAL, TRIMESTRAL, ARITMETICA, PONDERADA

    // Construtor padrão
    public ParametroEscola() {
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(UUID escolaId) {
        this.escolaId = escolaId;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
