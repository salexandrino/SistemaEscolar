package br.com.synge.academico.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HistoricoEscolarDTO(
        UUID idAluno,
        String nomeAluno,
        String cpf,
        String situacaoAtual,
        List<ItemMatriculaHistorico> matriculas
) {
    public record ItemMatriculaHistorico(
            UUID idTurma,
            String nomeTurma,
            String anoLetivo,
            String situacaoNaTurma,
            LocalDateTime dataAlteracao
    ) {}
}