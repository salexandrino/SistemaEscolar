package br.com.kutuar.seguranca.dtos;

import br.com.kutuar.seguranca.models.Escola;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {
    @Test void converteSomenteCamposDaListagem() {
        Escola escola = new Escola();
        UUID id = UUID.randomUUID();
        escola.setId(id);
        escola.setNome("Escola X");
        escola.setCnpj("12.345.678/0001-90");
        escola.setStatus("ATIVA");
        escola.setCidade("Recife");
        escola.setTenantId(UUID.randomUUID());
        escola.setEmailResponsavel("privado@example.com");
        var dto = EscolaResumoDTO.from(escola);
        assertEquals(new EscolaResumoDTO(id, "Escola X", "12.345.678/0001-90", "ATIVA", "Recife"), dto);
        var json = new ObjectMapper().valueToTree(new PageResponse<>(1, 10, 43, List.of(dto)));
        assertEquals(5, json.size());
        assertEquals(5, json.get("totalPages").asInt());
        var item = json.get("items").get(0);
        Set<String> fields = new HashSet<>();
        item.fieldNames().forEachRemaining(fields::add);
        assertEquals(Set.of("id", "nome", "cnpj", "status", "cidade"), fields);
        assertEquals(id.toString(), item.get("id").asText());
    }

    @Test void calculaTetoSemPerdaDePrecisao() {
        long[] totais = {0, 1, 10, 11, 43, Long.MAX_VALUE};
        long[] paginas = {0, 1, 1, 2, 5, 922337203685477581L};
        for (int i = 0; i < totais.length; i++) {
            assertEquals(paginas[i], new PageResponse<>(1, 10, totais[i], List.of()).totalPages());
        }
    }

    @Test void listaNuncaNulaENaoPodeSerAlteradaPeloChamador() {
        var vazio = new PageResponse<String>(1, 10, 0, null);
        assertNotNull(vazio.items());
        assertTrue(vazio.items().isEmpty());
        var origem = new ArrayList<>(List.of("item"));
        var pagina = new PageResponse<>(1, 10, 1, origem);
        origem.clear();
        assertEquals(List.of("item"), pagina.items());
        assertThrows(UnsupportedOperationException.class, () -> pagina.items().clear());
    }
}
