package tech.geracao.view.dtoResponse;

import org.eclipse.microprofile.openapi.annotations.media.Schema;


// ResponseUser — Modelo seguro da resposta de usuário, sem expor a senha armazenada.
@Schema(name = "ResponseUser", description = "Representa um usuário retornado pela API.")
public record ResponseUser(
        @Schema(description = "Identificador único do usuário", example = "1") Integer id,
        @Schema(description = "Primeiro nome do usuário", example = "João") String firstName,
        @Schema(description = "Sobrenome do usuário", example = "Silva") String surname,
        @Schema(description = "E-mail do usuário", example = "joao.silva@email.com") String email
) {
}
