package br.com.kutuar.academico.dtos;

public class AlterarSituacaoAlunoDTO {
    private String novaSituacao;
    private String motivo;

    public String getNovaSituacao() { return novaSituacao; }
    public void setNovaSituacao(String novaSituacao) { this.novaSituacao = novaSituacao; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
