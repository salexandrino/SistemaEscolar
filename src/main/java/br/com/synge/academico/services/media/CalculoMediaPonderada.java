package br.com.synge.academico.services.media;

import br.com.synge.academico.models.Avaliacao;
import br.com.synge.academico.models.Nota;

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

        Map<UUID, Avaliacao> mapAv = avaliacoes.stream()
                .collect(Collectors.toMap(Avaliacao::getId, a -> a));

        BigDecimal somaPesos = BigDecimal.ZERO;
        BigDecimal somaPonderada = BigDecimal.ZERO;

        for (Nota n : notas) {
            Avaliacao av = mapAv.get(n.getIdAvaliacao());
            if (av != null) {
                BigDecimal peso = av.getPeso() != null ? av.getPeso() : BigDecimal.ONE;
                somaPesos = somaPesos.add(peso);
                somaPonderada = somaPonderada.add(n.getValor().multiply(peso));
            }
        }

        if (somaPesos.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return somaPonderada.divide(somaPesos, 2, RoundingMode.HALF_UP);
    }
}
