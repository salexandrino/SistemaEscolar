package br.com.kutuar.seguranca.dtos;

import br.com.kutuar.seguranca.models.Escola;
import java.util.UUID;

/** Dados públicos necessários à listagem administrativa, sem relações ou dados internos. */
public record EscolaResumoDTO(UUID id, String nome, String cnpj, String status, String cidade) {
    public static EscolaResumoDTO from(Escola escola) {
        return new EscolaResumoDTO(escola.getId(), escola.getNome(), escola.getCnpj(),
                escola.getStatus(), escola.getCidade());
    }
}
