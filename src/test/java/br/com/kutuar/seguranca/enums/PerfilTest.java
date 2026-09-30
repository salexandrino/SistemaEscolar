package br.com.kutuar.seguranca.enums;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerfilTest {

    @Test
    void superAdminPossuiTodasAsPermissoes() {
        assertEquals(EnumSet.allOf(Permissao.class), Perfil.SUPER_ADMIN.getPermissoes());
    }

    @Test
    void perfisPossuemApenasAsPermissoesDefinidasNaMatriz() {
        assertEquals(Set.of(
                Permissao.ESCOLA_VISUALIZAR, Permissao.ESCOLA_EDITAR,
                Permissao.USUARIO_VISUALIZAR, Permissao.USUARIO_CRIAR, Permissao.USUARIO_EDITAR,
                Permissao.USUARIO_BLOQUEAR, Permissao.USUARIO_APROVAR,
                Permissao.ALUNO_VISUALIZAR, Permissao.ALUNO_CRIAR, Permissao.ALUNO_EDITAR,
                Permissao.MATRICULA_VISUALIZAR, Permissao.MATRICULA_CRIAR, Permissao.MATRICULA_EDITAR,
                Permissao.TURMA_VISUALIZAR, Permissao.TURMA_GERENCIAR,
                Permissao.NOTA_VISUALIZAR, Permissao.NOTA_LANCAR,
                Permissao.FREQUENCIA_VISUALIZAR, Permissao.FREQUENCIA_LANCAR,
                Permissao.FINANCEIRO_VISUALIZAR, Permissao.FINANCEIRO_GERENCIAR,
                Permissao.AUDITORIA_VISUALIZAR
        ), Perfil.GESTOR.getPermissoes());
        assertEquals(Set.of(
                Permissao.ESCOLA_VISUALIZAR, Permissao.USUARIO_VISUALIZAR,
                Permissao.ALUNO_VISUALIZAR, Permissao.ALUNO_CRIAR, Permissao.ALUNO_EDITAR,
                Permissao.MATRICULA_VISUALIZAR, Permissao.MATRICULA_CRIAR, Permissao.MATRICULA_EDITAR,
                Permissao.TURMA_VISUALIZAR
        ), Perfil.SECRETARIA.getPermissoes());
        assertEquals(Set.of(
                Permissao.ALUNO_VISUALIZAR, Permissao.MATRICULA_VISUALIZAR,
                Permissao.TURMA_VISUALIZAR, Permissao.NOTA_VISUALIZAR, Permissao.NOTA_LANCAR,
                Permissao.FREQUENCIA_VISUALIZAR, Permissao.FREQUENCIA_LANCAR
        ), Perfil.PROFESSOR.getPermissoes());
        assertEquals(Set.of(Permissao.FINANCEIRO_VISUALIZAR, Permissao.FINANCEIRO_GERENCIAR),
                Perfil.FINANCEIRO.getPermissoes());
    }

    @Test
    void verificaPermissaoTipadaEStringLegadaSemFallbackPorPrefixo() {
        assertTrue(Perfil.PROFESSOR.hasPermission(Permissao.NOTA_LANCAR));
        assertFalse(Perfil.PROFESSOR.hasPermission(Permissao.FINANCEIRO_VISUALIZAR));
        assertTrue(Perfil.FINANCEIRO.hasPermission("FINANCEIRO_GERENCIAR"));
        assertFalse(Perfil.FINANCEIRO.hasPermission("PAGAMENTO_EDITAR"));
    }
}
