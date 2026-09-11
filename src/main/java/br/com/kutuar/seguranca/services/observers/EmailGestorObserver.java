package br.com.kutuar.seguranca.services.observers;

import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.services.EmailService;

public class EmailGestorObserver implements EscolaCadastradaObserver {

    private final EmailService emailService;

    public EmailGestorObserver(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void aoCadastrarEscola(Escola escola, Usuario gestor, String senhaGerada) {
        emailService.enviarCredenciaisGestor(
                gestor.getEmail(), gestor.getNomeCompleto(), gestor.getCpf(), senhaGerada);
    }
}