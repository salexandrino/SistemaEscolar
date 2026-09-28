package br.com.kutuar.seguranca.enums;

import java.util.EnumSet;
import java.util.Set;

public enum Perfil {
    SUPER_ADMIN(EnumSet.allOf(Permissao.class)),

    GESTOR(EnumSet.of(
            Permissao.ESCOLA_VISUALIZAR,
            Permissao.ESCOLA_EDITAR,
            Permissao.USUARIO_VISUALIZAR,
            Permissao.USUARIO_CRIAR,
            Permissao.USUARIO_EDITAR,
            Permissao.USUARIO_BLOQUEAR,
            Permissao.USUARIO_APROVAR,
            Permissao.ALUNO_VISUALIZAR,
            Permissao.ALUNO_CRIAR,
            Permissao.ALUNO_EDITAR,
            Permissao.MATRICULA_VISUALIZAR,
            Permissao.MATRICULA_CRIAR,
            Permissao.MATRICULA_EDITAR,
            Permissao.TURMA_VISUALIZAR,
            Permissao.TURMA_GERENCIAR,
            Permissao.NOTA_VISUALIZAR,
            Permissao.NOTA_LANCAR,
            Permissao.FREQUENCIA_VISUALIZAR,
            Permissao.FREQUENCIA_LANCAR,
            Permissao.FINANCEIRO_VISUALIZAR,
            Permissao.FINANCEIRO_GERENCIAR,
            Permissao.AUDITORIA_VISUALIZAR
    )),

    SECRETARIA(EnumSet.of(
            Permissao.ESCOLA_VISUALIZAR,
            Permissao.USUARIO_VISUALIZAR,
            Permissao.ALUNO_VISUALIZAR,
            Permissao.ALUNO_CRIAR,
            Permissao.ALUNO_EDITAR,
            Permissao.MATRICULA_VISUALIZAR,
            Permissao.MATRICULA_CRIAR,
            Permissao.MATRICULA_EDITAR,
            Permissao.TURMA_VISUALIZAR
    )),

    PROFESSOR(EnumSet.of(
            Permissao.ALUNO_VISUALIZAR,
            Permissao.MATRICULA_VISUALIZAR,
            Permissao.TURMA_VISUALIZAR,
            Permissao.NOTA_VISUALIZAR,
            Permissao.NOTA_LANCAR,
            Permissao.FREQUENCIA_VISUALIZAR,
            Permissao.FREQUENCIA_LANCAR
    )),

    FINANCEIRO(EnumSet.of(
            Permissao.FINANCEIRO_VISUALIZAR,
            Permissao.FINANCEIRO_GERENCIAR
    ));

    private final Set<Permissao> permissoes;

    Perfil(Set<Permissao> permissoes) {
        this.permissoes = Set.copyOf(permissoes);
    }

    public boolean hasPermission(Permissao permissao) {
        return permissao != null && permissoes.contains(permissao);
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    /**
     * Ponte temporaria para consumidores que ainda fornecem o nome da permissao como texto.
     */
    @Deprecated
    public boolean hasPermission(String permission) {
        if (permission == null) {
            return false;
        }

        try {
            return hasPermission(Permissao.valueOf(permission));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
