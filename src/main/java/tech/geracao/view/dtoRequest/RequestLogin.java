package tech.geracao.view.dtoRequest;

// RequestLogin — Dados recebidos no login para identificar o usuário e validar sua senha.
import jakarta.validation.constraints.NotBlank;

public record RequestLogin(@NotBlank String email, @NotBlank String password) {
}
