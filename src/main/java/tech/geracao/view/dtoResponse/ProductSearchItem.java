package tech.geracao.view.dtoResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

// ProductSearchItem — Item de produto da busca, com retorno apenas dos campos solicitados.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductSearchItem(
        Integer id,
        Boolean enabled,
        String name,
        String slug,
        Integer stock,
        String description,
        Float price,
        @JsonProperty("price_with_discount") Float priceWithDiscount,
        @JsonProperty("category_ids") List<Integer> categoryIds,
        List<ProductResponse.ImageResponse> images,
        List<ProductResponse.OptionResponse> options
) {
    // Cria um item de busca mostrando somente os campos solicitados.
    public static ProductSearchItem from(ProductResponse product, Set<String> fields) {
        boolean includeAll = fields.isEmpty();

        return new ProductSearchItem(
                includeAll || fields.contains("id") ? product.id() : null,
                includeAll || fields.contains("enabled") ? product.enabled() : null,
                includeAll || fields.contains("name") ? product.name() : null,
                includeAll || fields.contains("slug") ? product.slug() : null,
                includeAll || fields.contains("stock") ? product.stock() : null,
                includeAll || fields.contains("description") ? product.description() : null,
                includeAll || fields.contains("price") ? product.price() : null,
                includeAll || fields.contains("price_with_discount") ? product.priceWithDiscount() : null,
                includeAll || fields.contains("category_ids") ? product.categoryIds() : null,
                includeAll || fields.contains("images") ? product.images() : null,
                includeAll || fields.contains("options") ? product.options() : null
        );
    }
}
