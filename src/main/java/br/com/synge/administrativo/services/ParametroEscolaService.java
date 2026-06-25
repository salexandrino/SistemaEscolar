package br.com.synge.administrativo.services;

import br.com.synge.administrativo.models.ParametroEscola;
import br.com.synge.administrativo.repositories.ParametroEscolaRepository;
import br.com.synge.seguranca.exceptions.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ParametroEscolaService {

    private static final Logger logger = LoggerFactory.getLogger(ParametroEscolaService.class);
    private final ParametroEscolaRepository repository;

    public ParametroEscolaService(ParametroEscolaRepository repository) {
        this.repository = repository;
    }

    public ParametroEscola save(ParametroEscola parametro) {
        // Validação de negócio, se necessário
        ParametroEscola savedParam = repository.save(parametro);
        logger.info("Parâmetro de escola salvo/atualizado: Chave={}, Valor={}", savedParam.getChave(), savedParam.getValor());
        return savedParam;
    }

    public Optional<ParametroEscola> findByEscolaIdAndChave(UUID escolaId, String chave) {
        return repository.findByEscolaIdAndChave(escolaId, chave);
    }

    public String getParametro(UUID escolaId, String chave) {
        return findByEscolaIdAndChave(escolaId, chave)
                .map(ParametroEscola::getValor)
                .orElseThrow(() -> new DomainException("Parâmetro " + chave + " não configurado para a escola.", HttpStatus.NOT_FOUND));
    }
}
