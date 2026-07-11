package br.com.synge.academico.services.media;

import br.com.synge.academico.models.Avaliacao;
import br.com.synge.academico.models.Nota;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculoMediaAritmetica implements CalculadoraMediaStrategy {

    @Override
    public BigDecimal calcular(List<Nota> notas, List<Avaliacao> avaliacoes) {
        if (notas == null || notas.isEmpty() || avaliacoes == null || avaliacoes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal soma = BigDecimal.ZERO;
        int count = 0;

        for (Nota n : notas) {
            soma = soma.add(n.getValor());
            count++;
        }

        if (count == 0) return BigDecimal.ZERO;
        return soma.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}
