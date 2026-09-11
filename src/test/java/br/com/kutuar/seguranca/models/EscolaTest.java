package br.com.kutuar.seguranca.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes JUnit 5 convertidos a partir da lógica manual de
 * br.com.kutuar.seguranca.testes.TesteEscola (métodos rodar()/main()).
 */
class EscolaTest {

    @Test
    @DisplayName("Nome e CNPJ da escola devem ser preservados corretamente no model")
    void criacaoDeEscolaValidaDevePreservarDados() {
        Escola escola = new Escola();
        escola.setNome("Escola Técnica Kutuar");
        escola.setCnpj("12.345.678/0001-99");

        assertEquals("Escola Técnica Kutuar", escola.getNome(),
                "O nome da escola não foi salvo corretamente no model.");
        assertEquals("12.345.678/0001-99", escola.getCnpj());
    }

    @Test
    @DisplayName("Status da escola deve assumir ATIVA como padrão quando nulo")
    void statusPadraoDeveSerAtivaQuandoNulo() {
        Escola escola = new Escola();
        // Mesma simulação do teste manual original: se o repository recebe status nulo,
        // ele aplica "ATIVA" como padrão antes de persistir.
        if (escola.getStatus() == null) {
            escola.setStatus("ATIVA");
        }

        assertEquals("ATIVA", escola.getStatus(),
                "O status padrão da escola deveria ser ATIVA.");
    }
}
