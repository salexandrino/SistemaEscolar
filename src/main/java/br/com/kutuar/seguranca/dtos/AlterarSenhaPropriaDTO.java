package br.com.kutuar.seguranca.dtos;

public class AlterarSenhaPropriaDTO {
    private String senhaAtual;
    private String novaSenha;
    private String confirmacaoNovaSenha;

    // Construtores
    public AlterarSenhaPropriaDTO() {}

    public AlterarSenhaPropriaDTO(String senhaAtual, String novaSenha, String confirmacaoNovaSenha) {
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
        this.confirmacaoNovaSenha = confirmacaoNovaSenha;
    }

    // Getters e Setters
    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public String getConfirmacaoNovaSenha() {
        return confirmacaoNovaSenha;
    }

    public void setConfirmacaoNovaSenha(String confirmacaoNovaSenha) {
        this.confirmacaoNovaSenha = confirmacaoNovaSenha;
    }
}
