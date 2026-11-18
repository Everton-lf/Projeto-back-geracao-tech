package tech.geracao.view.dtoRequest;

import java.util.List;

public record RequestProduct(
        Boolean enabled,
        String name,
        String slug,
        Integer stock,
        String description,
        Float price,
        Float priceWithDiscount,
        List<Integer> categoryIds,
        List<ImageRequest> images,
        List<OptionRequest> options
) {
    public record ImageRequest(Integer id, String content, String path, Boolean enabled) {}
    public record OptionRequest(Integer id, String title, String shape, Integer radius, String type, String values) {}
}
