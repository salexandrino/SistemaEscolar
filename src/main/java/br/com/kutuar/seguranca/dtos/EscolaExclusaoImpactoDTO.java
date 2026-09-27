package br.com.kutuar.seguranca.dtos;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resultado somente-leitura da análise prévia para uma futura exclusão de escola. */
public record EscolaExclusaoImpactoDTO(
        UUID escolaId,
        String nome,
        String status,
        boolean podeExcluir,
        List<String> motivosBloqueio,
        Map<String, Long> dependencias
) {
}
