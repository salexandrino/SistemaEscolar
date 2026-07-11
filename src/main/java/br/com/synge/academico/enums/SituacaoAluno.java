package br.com.synge.academico.enums;

import java.util.Set;

public enum SituacaoAluno {
    ATIVO,
    TRANCADO,
    TRANSFERIDO,
    CONCLUIDO,
    CANCELADO;

    /**
     * Valida se a transição de estado é permitida pela regra de negócio.
     */
    public boolean podeTransitarPara(SituacaoAluno nova) {
        return switch (this) {
            case ATIVO -> Set.of(TRANCADO, TRANSFERIDO, CONCLUIDO, CANCELADO).contains(nova);
            case TRANCADO -> Set.of(ATIVO, CANCELADO).contains(nova);
            case TRANSFERIDO, CONCLUIDO, CANCELADO -> false; // Estados finais
        };
    }

    public boolean isEstadoFinal() {
        return this == CONCLUIDO || this == CANCELADO || this == TRANSFERIDO;
    }
}