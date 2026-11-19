package tech.geracao.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import tech.geracao.service.ProductService;
import tech.geracao.view.dtoRequest.RequestProduct;
import tech.geracao.view.dtoResponse.ProductResponse;
import tech.geracao.view.dtoResponse.ProductSearchItem;
import tech.geracao.view.dtoResponse.ProductSearchResponse;
import tech.geracao.view.dtoResponse.ResponseUser;

import java.util.*;

// ProductController — Controlador REST que recebe buscas, filtros e operações de cadastro de produtos.
@Path("/v1/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductController {
    @Inject ProductService productService;

    @GET
    @Path("/search")
    // Interpreta filtros da URL, consulta produtos e prepara a resposta paginada.
    public ProductSearchResponse search(
            @QueryParam("limit") @DefaultValue("12") Integer limit,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("fields") String fields,
            @QueryParam("match") String match,
            @QueryParam("category_ids") String categoryIds,
            @QueryParam("price-range") String priceRange,
            @Context UriInfo uriInfo) {

        if (limit == null || (limit != -1 && limit < 1)) throw new BadRequestException("Limit inválido");
        int normalizedPage = page == null || page < 1 ? 1 : page;

        List<Integer> categories = parseIntegerList(categoryIds, "Lista de categorias inválida");
        Float[] priceWindow = parsePriceRange(priceRange);
        Map<Integer, List<String>> optionFilters = parseOptionFilters(uriInfo);

        List<ProductResponse> products = productService.listar(limit, normalizedPage, match, categories,
                priceWindow[0], priceWindow[1], optionFilters);
        long total = productService.contar(match, categories, priceWindow[0], priceWindow[1], optionFilters);

        Set<String> selected = normalizeFields(fields);
        List<ProductSearchItem> data = products.stream()
                .map(product -> ProductSearchItem.from(product, selected))
                .toList();
        return new ProductSearchResponse(data, total, limit,
                limit == -1 ? 1 : normalizedPage);
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Buscar produto por ID",
            description = "Retorna o produto correspondente ao ID informado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Produto retornado com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProductResponse.class)
            )
    )
    // Busca um produto pelo ID e devolve seus detalhes completos.
    public ProductResponse getById(@PathParam("id") Integer id) {
        return productService.buscarPorId(id);
    }
    @RolesAllowed("User")
    @POST
    @Operation(
            summary = "Criar produto",
            description = "Cria um novo produto no sistema."
    )
    @APIResponse(
            responseCode = "201",
            description = "Produto criado com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProductResponse.class)
            )
    )
    // Recebe os dados de um produto e solicita a sua criação.
    public Response create(RequestProduct request) {
        ProductResponse produtoCriado = productService.criar(request);
        return Response.status(Response.Status.CREATED).entity(produtoCriado).build();
    }
    @RolesAllowed("User")
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza as informações de um produto existente."
    )
    @APIResponse(
            responseCode = "200",
            description = "Produto atualizado com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProductResponse.class)
            )
    )
    // Recebe a alteração completa de um produto existente.
    public ProductResponse update(@PathParam("id") Integer id, RequestProduct request) {
        return productService.atualizar(id, request);
    }
    @RolesAllowed("User")
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Deletar produto",
            description = "Remove um produto existente do sistema."
    )
    @APIResponse(
            responseCode = "204",
            description = "Produto deletado com sucesso, sem corpo na resposta"
    )
    // Solicita a exclusão de um produto pelo ID.
    public Response delete(@PathParam("id") Integer id) {
        productService.deletar(id);
        return Response.noContent().build();
    }

    // Lê filtros no formato option[ID]=valores a partir da URL.
    private Map<Integer, List<String>> parseOptionFilters(UriInfo uriInfo) {
        Map<Integer, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
            if (!entry.getKey().startsWith("option[")) continue;
            String key = entry.getKey();
            int start = key.indexOf('['); int end = key.indexOf(']');
            if (start < 0 || end <= start + 1) throw new BadRequestException("Filtro de opção inválido");
            int optionId;
            try { optionId = Integer.parseInt(key.substring(start + 1, end)); }
            catch (NumberFormatException ex) { throw new BadRequestException("ID da opção inválido"); }
            List<String> values = new ArrayList<>();
            for (String raw : entry.getValue()) {
                for (String value : raw.split(",")) if (!value.trim().isEmpty()) values.add(value.trim());
            }
            if (values.isEmpty()) throw new BadRequestException("Valores da opção inválidos");
            result.put(optionId, values);
        }
        return result;
    }

    // Converte uma lista de números separados por vírgula.
    private List<Integer> parseIntegerList(String value, String message) {
        if (value == null || value.isBlank()) return List.of();
        List<Integer> ids = new ArrayList<>();
        try {
            for (String item : value.split("[,]")) ids.add(Integer.valueOf(item.trim()));
        } catch (NumberFormatException ex) { throw new BadRequestException(message); }
        if (ids.stream().anyMatch(id -> id < 1)) throw new BadRequestException(message);
        return ids;
    }

    // Valida e separa uma faixa de preços no formato mínimo-máximo.
    private Float[] parsePriceRange(String range) {
        if (range == null || range.isBlank()) return new Float[]{null, null};
        String[] parts = range.split("-", -1);
        if (parts.length != 2) throw new BadRequestException("Faixa de preço inválida");
        try {
            float min = Float.parseFloat(parts[0].trim());
            float max = Float.parseFloat(parts[1].trim());
            if (min < 0 || max < 0 || min > max) throw new BadRequestException("Faixa de preço inválida");
            return new Float[]{min, max};
        } catch (NumberFormatException ex) { throw new BadRequestException("Faixa de preço inválida"); }
    }

    // Converte a lista de campos solicitados em um conjunto normalizado.
    private Set<String> normalizeFields(String fields) {
        if (fields == null || fields.isBlank()) return Set.of();
        Set<String> selected = new LinkedHashSet<>();
        for (String field : fields.split(",")) {
            String normalized = field.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) selected.add(normalized);
        }
        return selected;
    }

}

