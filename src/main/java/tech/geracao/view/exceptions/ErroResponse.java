package tech.geracao.view.exceptions;

// ErroResponse — Modelo padronizado usado pelas respostas de erro da API.
import java.time.LocalDateTime;

public record ErroResponse(
        int status,
        String mensagem,
        LocalDateTime timestamp
) {}
