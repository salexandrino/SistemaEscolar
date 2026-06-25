package br.com.synge.seguranca.config;

import br.com.synge.seguranca.exceptions.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Manipula exceções de domínio/regra de negócio
    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Default, pode ser sobrescrito pela exceção
    public ModelAndView handleDomainException(DomainException ex, Model model) {
        logger.warn("Domain Exception: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        // Retorna para a página anterior ou uma página de erro genérica com a mensagem
        // Para fins de demonstração, vamos para a página de login se for um erro de autenticação
        // ou uma página de erro genérica para outros erros de domínio.
        if (ex.getStatus() == HttpStatus.UNAUTHORIZED) {
            return new ModelAndView("auth/login", model.asMap());
        }
        // Para outros erros de domínio, pode-se retornar a uma página de erro mais genérica
        // ou redirecionar para a página anterior com a mensagem.
        // Por enquanto, vamos usar a página de login como um exemplo de redirecionamento com erro.
        return new ModelAndView("error", model.asMap()); // Você precisaria criar um template error.html
    }

    // Manipula erros inesperados ou técnicos
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex, Model model) {
        logger.error("Internal Server Error: {}", ex.getMessage(), ex); // Loga o stack trace completo
        model.addAttribute("errorMessage", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde.");
        return new ModelAndView("error", model.asMap()); // Você precisaria criar um template error.html
    }
}
