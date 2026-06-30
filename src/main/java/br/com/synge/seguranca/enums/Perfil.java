package br.com.synge.seguranca.enums;

public enum Perfil {
    SUPER_ADMIN,
    GESTOR,
    SECRETARIA,
    PROFESSOR,
    FINANCEIRO;

    // Método para verificar se o perfil tem acesso a um determinado recurso/permissão
    public boolean hasPermission(String permission) {
        // Lógica de permissões aqui. Pode ser um switch, um mapa, ou uma enumeração de permissões.
        // Por enquanto, um placeholder.
        return switch (this) {
            case SUPER_ADMIN -> true; // Acesso total
            case GESTOR -> !permission.startsWith("SUPER_ADMIN_"); // Exemplo: Gestor não acessa coisas de SUPER_ADMIN
            case SECRETARIA -> permission.startsWith("ACADEMICO_") || permission.startsWith("ALUNO_");
            case PROFESSOR -> permission.startsWith("TURMA_") || permission.startsWith("ALUNO_") || permission.startsWith("NOTA_") || permission.startsWith("FREQUENCIA_");
            case FINANCEIRO -> permission.startsWith("PAGAMENTO_") || permission.startsWith("MENSALIDADE_") || permission.startsWith("RELATORIO_FINANCEIRO_");
            default -> false;
        };
    }
}
