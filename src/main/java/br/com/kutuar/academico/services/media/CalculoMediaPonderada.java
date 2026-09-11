package br.com.kutuar.academico.services.media;

import br.com.kutuar.academico.models.Avaliacao;
import br.com.kutuar.academico.models.Nota;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CalculoMediaPonderada implements CalculadoraMediaStrategy {

    @Override
    public BigDecimal calcular(List<Nota> notas, List<Avaliacao> avaliacoes) {
        if (notas == null || notas.isEmpty() || avaliacoes == null || avaliacoes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // CORREÇÃO: Avaliacao::id em vez de Avaliacao::getId
        Map<UUID, Avaliacao> mapAv = avaliacoes.stream()
                .collect(Collectors.toMap(Avaliacao::id, a -> a, (e, s) -> e));

        BigDecimal somaPesos = BigDecimal.ZERO;
        BigDecimal somaPonderada = BigDecimal.ZERO;

        for (Nota n : notas) {
            // CORREÇÃO: n.idAvaliacao() em vez de n.getIdAvaliacao()
            Avaliacao av = mapAv.get(n.idAvaliacao());
            if (av != null) {
                // CORREÇÃO: av.peso() e n.valor() sem o prefixo "get"
                BigDecimal peso = av.peso() != null ? av.peso() : BigDecimal.ONE;
                somaPesos = somaPesos.add(peso);
                somaPonderada = somaPonderada.add(n.valor().multiply(peso));
            }
        }

        if (somaPesos.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return somaPonderada.divide(somaPesos, 2, RoundingMode.HALF_UP);
    }
}