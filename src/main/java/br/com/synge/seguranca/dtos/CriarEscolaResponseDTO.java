package br.com.synge.seguranca.dtos;

import br.com.synge.seguranca.models.Escola;

/**
 * Resposta do cadastro de escola. Carrega a senha gerada para o Gestor em
 * texto puro APENAS nesta resposta (nunca fica salva em lugar nenhum) —
 * o Super Admin precisa copiar e repassar ao gestor agora, não dá pra
 * recuperar depois.
 */
public class CriarEscolaResponseDTO {

    private final Escola escola;
    private final String emailGestor;
    private final String cpfGestor;
    private final String senhaGeradaGestor;

    public CriarEscolaResponseDTO(Escola escola, String emailGestor, String cpfGestor, String senhaGeradaGestor) {
        this.escola = escola;
        this.emailGestor = emailGestor;
        this.cpfGestor = cpfGestor;
        this.senhaGeradaGestor = senhaGeradaGestor;
    }

    public Escola getEscola() {
        return escola;
    }

    public String getEmailGestor() {
        return emailGestor;
    }

    public String getCpfGestor() {
        return cpfGestor;
    }

    public String getSenhaGeradaGestor() {
        return senhaGeradaGestor;
    }
}