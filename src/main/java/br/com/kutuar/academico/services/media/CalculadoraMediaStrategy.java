package br.com.kutuar.academico.services.media;

import br.com.kutuar.academico.models.Avaliacao;
import br.com.kutuar.academico.models.Nota;

import java.math.BigDecimal;
import java.util.List;

public interface CalculadoraMediaStrategy {
    BigDecimal calcular(List<Nota> notas, List<Avaliacao> avaliacoes);
}
