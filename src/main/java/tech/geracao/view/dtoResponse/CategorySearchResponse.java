package tech.geracao.view.dtoResponse;

import java.util.List;
// CategorySearchResponse — Envelope da busca de categorias com dados selecionados e paginação.
import java.util.Map;

public record CategorySearchResponse(
        List<Map<String, Object>> data,
        long total,
        int limit,
        int page
) {
}
