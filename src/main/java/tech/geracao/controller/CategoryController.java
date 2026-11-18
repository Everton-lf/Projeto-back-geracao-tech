package tech.geracao.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.geracao.service.CategoryService;
import tech.geracao.view.dtoResponse.CategoryResponse;
import tech.geracao.view.dtoResponse.CategorySearchResponse;

import java.util.*;

@Path("/v1/category")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Categorias", description = "Operações relacionadas ao cadastro e consulta de categorias")
public class CategoryController {

    @Inject
    CategoryService categoryService;

    @GET
    @Operation(
            summary = "Listar Categorias",
            description = "Retorna a lista de categorias cadastradas no sistema."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de categorias retornada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CategorySearchResponse.class)
            )
    )
    @Path("/search")
    public CategorySearchResponse buscarCategorias(
            @QueryParam("limit") @DefaultValue("12") Integer limit,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("fields") String fields,
            @QueryParam("use_in_menu") Boolean useInMenu
    ) {
        int normalizedLimit = limit == null ? 12 : limit;
        int normalizedPage = page == null || page < 1 ? 1 : page;

        List<CategoryResponse> categorias = categoryService.listarCategorias(normalizedLimit, normalizedPage, useInMenu);
        long total = categoryService.contarCategorias(useInMenu);

        return new CategorySearchResponse(
                categorias.stream()
                        .map(categoria -> buildData(categoria, fields, useInMenu))
                        .toList(),
                total,
                normalizedLimit,
                limit != null && limit == -1 ? 1 : normalizedPage
        );
    }

    private Map<String, Object> buildData(CategoryResponse categoria, String fields, Boolean useInMenu) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        Set<String> selected = normalizeFields(fields);
        boolean includeAllFields = selected.isEmpty();

        if (selected.contains("id") || includeAllFields) {
            data.put("id", categoria.id());
        }
        if (selected.contains("name") || includeAllFields) {
            data.put("name", categoria.name());
        }
        if (selected.contains("slug") || includeAllFields) {
            data.put("slug", categoria.slug());
        }
        if (selected.contains("use_in_menu") || includeAllFields) {
            data.put("use_in_menu", categoria.useInMenu());
        }

        return data;
    }

    private Set<String> normalizeFields(String fields) {
        if (fields == null || fields.isBlank()) {
            return new LinkedHashSet<>();
        }

        Set<String> selected = new LinkedHashSet<>();
        for (String field : fields.split(",")) {
            String normalized = field.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                selected.add(normalized);
            }
        }

        return selected;
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Buscar Categoria por ID",
            description = "Retorna os detalhes de uma categoria específica pelo ID."
    )
    @APIResponse(
            responseCode = "200",
            description = "Categoria encontrada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CategorySearchResponse.class)
            )
    )
    public CategoryResponse buscarCategoriaPorId(@PathParam("id") Integer id) {
        return categoryService.buscarCategoriaPorId(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Deletar Categoria",
            description = "Remove uma categoria específica do sistema."
    )
    @APIResponse(
            responseCode = "200",
            description = "Categoria deletada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CategorySearchResponse.class)
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Categoria deletada com sucesso, sem conteúdo de resposta"

    )
    public void deletarCategoria(@PathParam("id") Integer id) {
        categoryService.deletarCategoria(id);
    }

    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Atualizar Categoria",
            description = "Atualiza os detalhes de uma categoria específica."
    )
    @APIResponse(
            responseCode = "200",
            description = "Categoria atualizada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CategorySearchResponse.class)
            )
    )
    public CategoryResponse atualizarCategoria(@PathParam("id") Integer id, CategoryResponse categoryResponse) {
        return categoryService.editarCategoria(id, categoryResponse);
    }

    @POST
    @Operation(
            summary = "Criar Categoria",
            description = "Cria uma nova categoria no sistema."
    )
    @APIResponse(
            responseCode = "201",
            description = "Categoria criada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CategorySearchResponse.class)
            )
    )
    public CategoryResponse criarCategoria(CategoryResponse categoryResponse) {
        return categoryService.criarCategoria(categoryResponse);
    }


}
