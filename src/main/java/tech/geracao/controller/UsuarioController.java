package tech.geracao.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.geracao.view.dtoRequest.RequestLogin;
import tech.geracao.view.dtoResponse.ResponseToken;
import tech.geracao.view.exceptions.ErroResponse;
import tech.geracao.view.dtoRequest.RequestUser;
import tech.geracao.view.dtoResponse.ResponseUser;
import tech.geracao.service.UsuarioService;

import java.util.List;

// UsuarioController — Controlador REST responsável pelos endpoints de autenticação e gerenciamento de usuários.
@Path("/v1/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuários", description = "Operações relacionadas ao cadastro e consulta de usuários")
public class UsuarioController {

    @Inject
    UsuarioService usuarioService;


    @PermitAll
    @POST
    @Path("/token")
    @Operation(
            summary = "Gerar token",
            description = "Retorna um token de acesso para o usuário."
    )
    @APIResponse(
            responseCode = "200",
            description = "Token gerado com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ResponseToken.class)
            )
    )
    @APIResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    // Valida as credenciais e gera um token JWT válido por duas horas.
    public ResponseToken gerarToken(RequestLogin request) {
        return usuarioService.gerarToken(request);
    }
    @GET
    @Operation(
            summary = "Listar usuários",
            description = "Retorna a lista de usuários cadastrados no sistema."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de usuários retornada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ResponseUser.class)
            )
    )
    // Consulta todos os usuários e remove dados sensíveis da resposta.
    public List<ResponseUser> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Listar usuários por Id",
            description = "Retorna um usuário cadastrado no sistema por id."
    )
    @APIResponse(
            responseCode = "200",
            description = "Mostra o usuário por Id",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ResponseUser.class)
            )
    )
    @APIResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseUser listarUsuariosPorId(@PathParam("id") Integer id) {
        return usuarioService.usuarioPorId(id);
    }


    @RolesAllowed("User")
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Atualizar usuário por Id",
            description = "Atualiza as informações de um usuário cadastrado no sistema por id."
    )
    @APIResponse(
            responseCode = "200",
            description = "Usuário atualizado com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ResponseUser.class)
            )
    )
    @APIResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseUser atualizarUsuarioPorId(@PathParam("id") Integer id, @Valid RequestUser usuario) {

        return usuarioService.editarUsuario(id, usuario);
    }
    @PermitAll
    @POST
    @APIResponse(responseCode = "201", description = "Usuário criado com sucesso",
            content = @Content(schema = @Schema(implementation = RequestUser.class)))
    @APIResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    // Verifica duplicidade, protege a senha e cria o usuário.
    public Response criarUsuario(@Valid RequestUser request) {
        ResponseUser usuarioCriado = usuarioService.criarUsuario(request);
        return Response.status(Response.Status.CREATED)
                .entity(usuarioCriado)
                .build();
    }
    @RolesAllowed("User")
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Deletar usuário por Id",
            description = "Deleta um usuário cadastrado no sistema por id."
    )
    @APIResponse(
            responseCode = "204",
            description = "Usuário deletado com sucesso, sem corpo na resposta"
    )
    @APIResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    // Localiza e remove um usuário existente.
    public Response deletarUsuario(@PathParam("id") Integer id) {
        usuarioService.deletarUsuario(id);
        return Response.noContent().build();
    }

}