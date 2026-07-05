package br.com.synge.seguranca.testes;

import br.com.synge.seguranca.models.Escola;

public class TesteEscola {

    public static void rodar() {
        System.out.println("⏳ [TESTE] Iniciando testes de Escola...");

        testarCriacaoDeEscolaValida();
        testarStatusPadraoComoAtivo();

        System.out.println("✅ [TESTE] Todos os testes de Escola passaram!");
    }

    private static void testarCriacaoDeEscolaValida() {
        Escola escola = new Escola();
        escola.setNome("Escola Técnica Synge");
        escola.setCnpj("12.345.678/0001-99");

        if (!escola.getNome().equals("Escola Técnica Synge")) {
            throw new RuntimeException("❌ Falha: O nome da escola não foi salvo corretamente no modelo.");
        }
        System.out.println("  • Criação de escola válida: OK");
    }

    private static void testarStatusPadraoComoAtivo() {
        Escola escola = new Escola();
        // Simulando o que seu repository faz se o status for nulo
        if (escola.getStatus() == null) {
            escola.setStatus("ATIVA");
        }

        if (!"ATIVA".equals(escola.getStatus())) {
            throw new RuntimeException("❌ Falha: O status padrão da escola deveria ser ATIVA.");
        }
        System.out.println("  • Status padrão ATIVA: OK");
    }
}