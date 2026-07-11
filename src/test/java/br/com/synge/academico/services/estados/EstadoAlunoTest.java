package br.com.synge.academico.services.estados;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoAlunoTest {

    @Test
    @DisplayName("Deve permitir transições a partir do estado ATIVO")
    void transicoesDeAtivo() {
        assertTrue(SituacaoAluno.ATIVO.podeMudarPara(SituacaoAluno.TRANCADO));
        assertTrue(SituacaoAluno.ATIVO.podeMudarPara(SituacaoAluno.TRANSFERIDO));
        assertTrue(SituacaoAluno.ATIVO.podeMudarPara(SituacaoAluno.CONCLUIDO));
        assertTrue(SituacaoAluno.ATIVO.podeMudarPara(SituacaoAluno.CANCELADO));
        assertFalse(SituacaoAluno.ATIVO.podeMudarPara(SituacaoAluno.ATIVO));
    }

    @Test
    @DisplayName("Deve permitir transições a partir do estado TRANCADO")
    void transicoesDeTrancado() {
        assertTrue(SituacaoAluno.TRANCADO.podeMudarPara(SituacaoAluno.ATIVO));
        assertTrue(SituacaoAluno.TRANCADO.podeMudarPara(SituacaoAluno.TRANSFERIDO));
        assertTrue(SituacaoAluno.TRANCADO.podeMudarPara(SituacaoAluno.CANCELADO));
        assertFalse(SituacaoAluno.TRANCADO.podeMudarPara(SituacaoAluno.CONCLUIDO));
    }

    @Test
    @DisplayName("Não deve permitir nenhuma transição a partir do estado TRANSFERIDO")
    void transicoesDeTransferido() {
        assertFalse(SituacaoAluno.TRANSFERIDO.podeMudarPara(SituacaoAluno.ATIVO));
        assertFalse(SituacaoAluno.TRANSFERIDO.podeMudarPara(SituacaoAluno.TRANCADO));
        assertFalse(SituacaoAluno.TRANSFERIDO.podeMudarPara(SituacaoAluno.CONCLUIDO));
        assertFalse(SituacaoAluno.TRANSFERIDO.podeMudarPara(SituacaoAluno.CANCELADO));
    }

    @Test
    @DisplayName("Não deve permitir nenhuma transição a partir do estado CONCLUIDO")
    void transicoesDeConcluido() {
        assertFalse(SituacaoAluno.CONCLUIDO.podeMudarPara(SituacaoAluno.ATIVO));
        assertFalse(SituacaoAluno.CONCLUIDO.podeMudarPara(SituacaoAluno.TRANCADO));
        assertFalse(SituacaoAluno.CONCLUIDO.podeMudarPara(SituacaoAluno.TRANSFERIDO));
        assertFalse(SituacaoAluno.CONCLUIDO.podeMudarPara(SituacaoAluno.CANCELADO));
    }

    @Test
    @DisplayName("Deve permitir reativar a partir do estado CANCELADO")
    void transicoesDeCancelado() {
        assertTrue(SituacaoAluno.CANCELADO.podeMudarPara(SituacaoAluno.ATIVO));
        assertFalse(SituacaoAluno.CANCELADO.podeMudarPara(SituacaoAluno.TRANCADO));
        assertFalse(SituacaoAluno.CANCELADO.podeMudarPara(SituacaoAluno.TRANSFERIDO));
        assertFalse(SituacaoAluno.CANCELADO.podeMudarPara(SituacaoAluno.CONCLUIDO));
    }
}
