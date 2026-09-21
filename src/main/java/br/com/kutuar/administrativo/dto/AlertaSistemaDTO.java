package br.com.kutuar.administrativo.dto;

public class AlertaSistemaDTO {
    private String tipo;
    private String severidade;
    private String mensagem;
    private long quantidade;
    private String link;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getSeveridade() { return severidade; }
    public void setSeveridade(String severidade) { this.severidade = severidade; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public long getQuantidade() { return quantidade; }
    public void setQuantidade(long quantidade) { this.quantidade = quantidade; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
