package tech.geracao.view.exceptions;

import java.time.LocalDateTime;

public record ErroResponse(
        int status,
        String mensagem,
        LocalDateTime timestamp
) {}
