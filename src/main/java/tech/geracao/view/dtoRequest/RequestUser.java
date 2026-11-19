package tech.geracao.view.dtoRequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// RequestUser — Dados enviados ao cadastrar ou atualizar um usuário.
public record RequestUser(
    @NotBlank(message = "O nome é obrigatório") String firstName,
    @NotBlank(message = "O sobrenome é obrigatório") String surname,
    @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail deve ser válido") String email,
    @NotBlank(message = "A senha é obrigatória") String password
) {}

