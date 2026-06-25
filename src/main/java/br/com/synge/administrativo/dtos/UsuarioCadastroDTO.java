package br.com.synge.administrativo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UsuarioCadastroDTO(
        UUID id, // Para atualização
        UUID escolaId, // Para multi-tenant
        @NotBlank(message = "O CPF não pode estar em branco.")
        @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "CPF deve estar no formato 000.000.000-00.")
        String cpf,
        @NotBlank(message = "A senha não pode estar em branco.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String senha,
        @NotBlank(message = "O nome não pode estar em branco.")
        String nome,
        @NotBlank(message = "O email não pode estar em branco.")
        @Email(message = "Formato de email inválido.")
        String email,
        @NotBlank(message = "A função (role) não pode estar em branco.")
        String role,
        Boolean ativo
) {}
