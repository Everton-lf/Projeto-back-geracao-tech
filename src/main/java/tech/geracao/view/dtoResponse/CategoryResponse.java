package tech.geracao.view.dtoResponse;

public record CategoryResponse(
        Integer id,
        String name,
        String slug,
        Boolean useInMenu
) {
}
