package br.com.kutuar.seguranca.services;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Envio de e-mails via SMTP (recuperação de senha, e futuramente outras
 * notificações). Configuração vem de variáveis de ambiente / .env, mesmo
 * padrão já usado em DatabaseConfig/JwtService.
 *
 * Se SMTP_HOST não estiver configurado, cai em modo "desenvolvimento":
 * não tenta enviar de verdade, só loga o conteúdo — assim o projeto
 * continua funcionando localmente sem exigir credenciais de e-mail reais.
 */
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final String host;
    private final String port;
    private final String user;
    private final String password;
    private final String from;
    private final boolean configurado;

    public EmailService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        this.host = valor("SMTP_HOST", dotenv);
        this.port = valorOuPadrao("SMTP_PORT", dotenv, "587");
        this.user = valor("SMTP_USER", dotenv);
        this.password = valor("SMTP_PASSWORD", dotenv);
        this.from = valorOuPadrao("SMTP_FROM", dotenv, this.user);

        this.configurado = host != null && !host.isBlank() && user != null && !user.isBlank();

        if (!configurado) {
            logger.warn("SMTP não configurado (SMTP_HOST/SMTP_USER ausentes). " +
                    "E-mails vão apenas ser logados no console, não enviados de verdade.");
        }
    }

    private String valor(String chave, Dotenv dotenv) {
        String v = System.getenv(chave);
        if (v == null || v.isBlank()) {
            v = dotenv.get(chave);
        }
        return v;
    }

    private String valorOuPadrao(String chave, Dotenv dotenv, String padrao) {
        String v = valor(chave, dotenv);
        return (v == null || v.isBlank()) ? padrao : v;
    }

    public void enviarCodigoRecuperacaoSenha(String destinatario, String nomeUsuario, String codigo) {
        String assunto = "SYNGE — Código para redefinir sua senha";
        String corpo = "Olá" + (nomeUsuario != null ? ", " + nomeUsuario : "") + ",\n\n"
                + "Recebemos uma solicitação para redefinir sua senha no SYNGE.\n\n"
                + "Seu código de verificação é: " + codigo + "\n\n"
                + "Esse código expira em 15 minutos. Se você não solicitou essa alteração, "
                + "pode ignorar este e-mail com segurança — sua senha atual continua válida.\n\n"
                + "Equipe SYNGE";

        if (!configurado) {
            // Modo desenvolvimento: sem SMTP configurado, só loga (não quebra o fluxo).
            logger.info("[E-MAIL SIMULADO] Para: {} | Assunto: {} | Código: {}", destinatario, assunto, codigo);
            return;
        }

        try {
            enviar(destinatario, assunto, corpo);
            logger.info("E-mail de recuperação de senha enviado para {}.", destinatario);
        } catch (MessagingException e) {
            // Não relança: falha no envio de e-mail não deve travar o fluxo de
            // "esqueci minha senha" nem revelar detalhes técnicos ao usuário.
            // O código já foi salvo no banco por quem chamou este método.
            logger.error("Falha ao enviar e-mail de recuperação para {}: {}", destinatario, e.getMessage(), e);
        }
    }

    public void enviarCredenciaisGestor(String destinatario, String nomeGestor, String cpf, String senhaTemporaria) {
        String assunto = "SYNGE — Acesso do Gestor criado";
        String corpo = "Olá" + (nomeGestor != null ? ", " + nomeGestor : "") + ",\n\n"
                + "Uma conta de Gestor foi criada para você no SYNGE.\n\n"
                + "CPF de login: " + cpf + "\n"
                + "Senha temporária: " + senhaTemporaria + "\n\n"
                + "Por segurança, altere essa senha assim que fizer o primeiro acesso.\n\n"
                + "Equipe SYNGE";

        if (!configurado) {
            logger.info("[E-MAIL SIMULADO] Para: {} | Assunto: {} | CPF: {}", destinatario, assunto, cpf);
            return;
        }

        try {
            enviar(destinatario, assunto, corpo);
            logger.info("E-mail de credenciais de Gestor enviado para {}.", destinatario);
        } catch (MessagingException e) {
            logger.error("Falha ao enviar e-mail de credenciais para {}: {}", destinatario, e.getMessage(), e);
        }
    }

    private void enviar(String destinatario, String assunto, String corpo) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(assunto);
        message.setText(corpo);

        Transport.send(message);
    }
}