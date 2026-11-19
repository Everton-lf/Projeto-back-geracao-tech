package tech.geracao.view.exceptions;

import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;

// UnauthorizedExceptionMapper — Converte erros de autorização em respostas JSON claras.
@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {


    @Override
    // Converte uma entidade no formato seguro utilizado pela API.
    public Response toResponse(UnauthorizedException exception) {
        String mensagem = (exception.getMessage() != null && !exception.getMessage().isBlank()) ? exception.getMessage() : "Token de autorização ausente ou inválido";
        ErroResponse erro = new ErroResponse(
                Response.Status.UNAUTHORIZED.getStatusCode(),
                mensagem,
                LocalDateTime.now()
        );

        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(erro)
                .build();
    }
}