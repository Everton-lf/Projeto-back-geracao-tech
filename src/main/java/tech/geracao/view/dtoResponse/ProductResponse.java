package tech.geracao.view.dtoResponse;

import java.util.List;

public record ProductResponse(
        Integer id,
        Boolean enabled,
        String name,
        String slug,
        Integer stock,
        String description,
        Float price,
        Float priceWithDiscount,
        List<Integer> categoryIds,
        List<ImageResponse> images,
        List<OptionResponse> options
) {
    public record ImageResponse(Integer id, String content) {}
    public record OptionResponse(Integer id, String title, String shape, Integer radius, String type, String values) {}
}
