// CategoryResponse — Representação de uma categoria retornada pela API.
package tech.geracao.view.dtoResponse;

public record CategoryResponse(
        Integer id,
        String name,
        String slug,
        Boolean useInMenu
) {
}
