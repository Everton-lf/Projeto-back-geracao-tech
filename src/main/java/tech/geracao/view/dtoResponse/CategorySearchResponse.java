package tech.geracao.view.dtoResponse;

import java.util.List;
import java.util.Map;

public record CategorySearchResponse(
        List<Map<String, Object>> data,
        long total,
        int limit,
        int page
) {
}
