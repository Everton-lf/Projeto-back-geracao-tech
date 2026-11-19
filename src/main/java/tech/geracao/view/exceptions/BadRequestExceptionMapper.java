package tech.geracao.view.exceptions;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;

// Mapeia erros de requisição inválida para respostas HTTP 400 padronizadas.
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    @Override
    public Response toResponse(BadRequestException exception) {
        String mensagem = exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Requisição inválida"
                : exception.getMessage();
        ErroResponse erro = new ErroResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                mensagem,
                LocalDateTime.now()
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(erro)
                .build();
    }
}
