package br.com.synge.academico.dtos;

import java.util.UUID;

public class TransferirAlunoDTO {
    private UUID idNovaTurma;
    private String motivo;

    public UUID getIdNovaTurma() { return idNovaTurma; }
    public void setIdNovaTurma(UUID idNovaTurma) { this.idNovaTurma = idNovaTurma; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
