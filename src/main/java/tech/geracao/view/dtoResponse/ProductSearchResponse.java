package tech.geracao.view.dtoResponse;

// ProductSearchResponse — Envelope da busca de produtos com dados, total e paginação.
import java.util.List;

public record ProductSearchResponse(
        List<ProductSearchItem> data,
        long total,
        int limit,
        int page
) {}
