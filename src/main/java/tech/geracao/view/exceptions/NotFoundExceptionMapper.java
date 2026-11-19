package tech.geracao.view.exceptions;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;

// NotFoundExceptionMapper — Converte recursos não encontrados em respostas JSON com status 404.
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    // Converte uma entidade no formato seguro utilizado pela API.
    public Response toResponse(NotFoundException exception) {
        ErroResponse erro = new ErroResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(erro)
                .build();
    }
}
