package br.com.synge.academico.services.media;

import br.com.synge.academico.models.Avaliacao;
import br.com.synge.academico.models.Nota;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraMediaTest {

    @Test
    @DisplayName("Deve calcular média aritmética corretamente")
    void calcularMediaAritmetica() {
        CalculadoraMediaStrategy calc = new CalculoMediaAritmetica();

        UUID av1Id = UUID.randomUUID();
        UUID av2Id = UUID.randomUUID();

        Avaliacao av1 = new Avaliacao(av1Id, null, null, null, null, null, null, null);
        Avaliacao av2 = new Avaliacao(av2Id, null, null, null, null, null, null, null);

        Nota n1 = new Nota(null, null, av1Id, null, new BigDecimal("7.0"), null, null);
        Nota n2 = new Nota(null, null, av2Id, null, new BigDecimal("8.0"), null, null);

        BigDecimal media = calc.calcular(Arrays.asList(n1, n2), Arrays.asList(av1, av2));

        assertEquals(new BigDecimal("7.50"), media);
    }

    @Test
    @DisplayName("Deve calcular média ponderada corretamente")
    void calcularMediaPonderada() {
        CalculadoraMediaStrategy calc = new CalculoMediaPonderada();

        UUID av1Id = UUID.randomUUID();
        UUID av2Id = UUID.randomUUID();

        Avaliacao av1 = new Avaliacao(av1Id, null, null, null, null, new BigDecimal("2.0"), null, null);
        Avaliacao av2 = new Avaliacao(av2Id, null, null, null, null, new BigDecimal("3.0"), null, null);

        // Nota 1 (peso 2): 7.0 -> 14
        // Nota 2 (peso 3): 8.0 -> 24
        // Soma ponderada: 38. Soma pesos: 5. Média: 38 / 5 = 7.6
        Nota n1 = new Nota(null, null, av1Id, null, new BigDecimal("7.0"), null, null);
        Nota n2 = new Nota(null, null, av2Id, null, new BigDecimal("8.0"), null, null);

        BigDecimal media = calc.calcular(Arrays.asList(n1, n2), Arrays.asList(av1, av2));

        assertEquals(new BigDecimal("7.60"), media);
    }

    @Test
    @DisplayName("Deve retornar ZERO se não houver notas")
    void semNotas() {
        CalculadoraMediaStrategy calcA = new CalculoMediaAritmetica();
        CalculadoraMediaStrategy calcP = new CalculoMediaPonderada();

        assertEquals(BigDecimal.ZERO, calcA.calcular(Collections.emptyList(), Collections.emptyList()));
        assertEquals(BigDecimal.ZERO, calcP.calcular(Collections.emptyList(), Collections.emptyList()));
    }
}