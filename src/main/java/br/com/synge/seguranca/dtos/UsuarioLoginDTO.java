package br.com.synge.seguranca.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioLoginDTO(
        @NotBlank(message = "O CPF não pode estar em branco.")
        @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "CPF deve estar no formato 000.000.000-00.")
        String cpf,

        @NotBlank(message = "A senha não pode estar em branco.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String senha
) {}
