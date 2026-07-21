package br.com.synge.academico.services.media;

import br.com.synge.academico.models.Avaliacao;
import br.com.synge.academico.models.Nota;

import java.math.BigDecimal;
import java.util.List;

public interface CalculadoraMediaStrategy {
    BigDecimal calcular(List<Nota> notas, List<Avaliacao> avaliacoes);
}
