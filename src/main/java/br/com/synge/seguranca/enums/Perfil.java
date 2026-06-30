package br.com.synge.seguranca.enums;

public enum Perfil {
    SUPER_ADMIN,
    GESTOR,
    SECRETARIA,
    PROFESSOR,
    FINANCEIRO;

    // Método para verificar se o perfil tem acesso a um determinado recurso/permissão
    public boolean hasPermission(String permission) {
        // Esta lógica é um placeholder e deve ser expandida com um sistema de permissões mais granular
        // Exemplo: permission pode ser "ACADEMICO_VIEW", "FINANCEIRO_EDIT", "USUARIO_CREATE"
        return switch (this) {
            case SUPER_ADMIN -> true; // Acesso total
            case GESTOR -> !permission.startsWith("SUPER_ADMIN_"); // Gestor não acessa funcionalidades de SUPER_ADMIN
            case SECRETARIA -> permission.startsWith("ACADEMICO_") || permission.startsWith("ALUNO_") || permission.startsWith("TURMA_");
            case PROFESSOR -> permission.startsWith("PROFESSOR_") || permission.startsWith("NOTA_") || permission.startsWith("FREQUENCIA_");
            case FINANCEIRO -> permission.startsWith("FINANCEIRO_") || permission.startsWith("PAGAMENTO_") || permission.startsWith("MENSALIDADE_");
            default -> false;
        };
    }
}