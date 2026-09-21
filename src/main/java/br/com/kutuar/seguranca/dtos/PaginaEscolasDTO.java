package br.com.kutuar.seguranca.dtos;

import br.com.kutuar.seguranca.models.Escola;
import java.util.List;

public record PaginaEscolasDTO(List<Escola> itens, int page, int size, long total,
                               long totalPages, boolean hasPrevious, boolean hasNext) {
}
