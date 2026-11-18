package tech.geracao.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import tech.geracao.model.*;
import tech.geracao.repository.RepositoryProduct;
import tech.geracao.repository.RepositoryCategory;
import tech.geracao.view.dtoRequest.RequestProduct;
import tech.geracao.view.dtoResponse.ProductResponse;

import java.util.*;

@ApplicationScoped
public class ProductService {
    @Inject RepositoryProduct productRepository;
    @Inject RepositoryCategory categoryRepository;

    @Transactional
    public List<ProductResponse> listar(Integer limit, int page, String match,
                                       List<Integer> categoryIds, Float minPrice, Float maxPrice,
                                       Map<Integer, List<String>> optionFilters) {
        StringBuilder jpql = new StringBuilder("1 = 1");
        Map<String, Object> params = new HashMap<>();
        int index = 1;

        if (match != null && !match.isBlank()) {
            jpql.append(" and (lower(p.name) like :match or lower(p.description) like :match)");
            params.put("match", "%" + match.trim().toLowerCase(Locale.ROOT) + "%");
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            jpql.append(" and exists (select c from EntityCategory c join c.products cp where c.id in (:categories" + index + ") and cp = p)");
            params.put("categories" + index++, categoryIds);
        }
        if (minPrice != null) {
            jpql.append(" and p.price >= :minPrice"); params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            jpql.append(" and p.price <= :maxPrice"); params.put("maxPrice", maxPrice);
        }
        for (Map.Entry<Integer, List<String>> entry : optionFilters.entrySet()) {
            String optionParam = "optionValues" + index;
            String optionIdParam = "optionId" + index;
            jpql.append(" and exists (select o from EntityProductOption o where o.product = p and o.id = :").append(optionIdParam)
                    .append(" and (");
            List<String> values = entry.getValue();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) jpql.append(" or ");
                String valueParam = optionParam + i;
                jpql.append("lower(o.values) like :").append(valueParam);
                params.put(valueParam, "%" + values.get(i).trim().toLowerCase(Locale.ROOT) + "%");
            }
            jpql.append("))");
            params.put(optionIdParam, entry.getKey());
            index++;
        }

        PanacheQuery<EntityProduct> query = EntityProduct.find(jpql.toString(), Sort.by("id").ascending(), params);
        if (limit != -1) query.page(Page.of(Math.max(page - 1, 0), limit));
        return query.list().stream().map(this::toResponse).toList();
    }

    public long contar(String match, List<Integer> categoryIds, Float minPrice, Float maxPrice,
                       Map<Integer, List<String>> optionFilters) {
        StringBuilder jpql = new StringBuilder("1 = 1");
        Map<String, Object> params = new HashMap<>();
        int index = 1;
        if (match != null && !match.isBlank()) {
            jpql.append(" and (lower(p.name) like :match or lower(p.description) like :match)");
            params.put("match", "%" + match.trim().toLowerCase(Locale.ROOT) + "%");
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            jpql.append(" and exists (select c from EntityCategory c join c.products cp where c.id in (:categories" + index + ") and cp = p)");
            params.put("categories" + index++, categoryIds);
        }
        if (minPrice != null) { jpql.append(" and p.price >= :minPrice"); params.put("minPrice", minPrice); }
        if (maxPrice != null) { jpql.append(" and p.price <= :maxPrice"); params.put("maxPrice", maxPrice); }
        for (Map.Entry<Integer, List<String>> entry : optionFilters.entrySet()) {
            String optionIdParam = "optionIdCount" + index;
            jpql.append(" and exists (select o from EntityProductOption o where o.product = p and o.id = :").append(optionIdParam).append(" and (");
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (i > 0) jpql.append(" or ");
                String valueParam = "optionValueCount" + index + i;
                jpql.append("lower(o.values) like :").append(valueParam);
                params.put(valueParam, "%" + entry.getValue().get(i).trim().toLowerCase(Locale.ROOT) + "%");
            }
            jpql.append("))"); params.put(optionIdParam, entry.getKey()); index++;
        }
        return EntityProduct.count(jpql.toString(), params);
    }

    public ProductResponse buscarPorId(Integer id) {
        EntityProduct product = productRepository.findById(id);
        if (product == null) throw new NotFoundException("Produto não encontrado");
        return toResponse(product);
    }

    @Transactional
    public ProductResponse criar(RequestProduct request) {
        validateRequest(request);
        EntityProduct product = new EntityProduct();
        apply(product, request);
        productRepository.persist(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse atualizar(Integer id, RequestProduct request) {
        EntityProduct product = productRepository.findById(id);
        if (product == null) throw new NotFoundException("Produto não encontrado");
        validateRequest(request);
        apply(product, request);
        return toResponse(product);
    }

    @Transactional
    public void deletar(Integer id) {
        EntityProduct product = productRepository.findById(id);
        if (product == null) throw new NotFoundException("Produto não encontrado");
        productRepository.delete(product);
    }

    private void validateRequest(RequestProduct request) {
        if (request == null || request.name() == null || request.name().isBlank())
            throw new BadRequestException("Nome do produto é obrigatório");
        if (request.slug() == null || request.slug().isBlank())
            throw new BadRequestException("Slug do produto é obrigatório");
        if (request.price() == null || request.priceWithDiscount() == null)
            throw new BadRequestException("Preço e preço com desconto são obrigatórios");
    }

    private void apply(EntityProduct product, RequestProduct request) {
        product.enabled = request.enabled() != null ? request.enabled() : false;
        product.name = request.name(); product.slug = request.slug();
        product.stock = request.stock() != null ? request.stock() : 0;
        product.description = request.description(); product.price = request.price();
        product.priceWithDiscount = request.priceWithDiscount();

        if (request.categoryIds() != null) {
            product.categories = new ArrayList<>();
            for (Integer categoryId : request.categoryIds()) {
                EntityCategory category = categoryRepository.findById(categoryId);
                if (category == null) throw new BadRequestException("Categoria inválida: " + categoryId);
                product.categories.add(category);
            }
        }
        if (product.images == null) product.images = new ArrayList<>();
        if (request.images() != null) {
            product.images.clear();
            for (RequestProduct.ImageRequest imageRequest : request.images()) {
                EntityProductImage image = new EntityProductImage();
                image.product = product; image.enabled = imageRequest.enabled() != null ? imageRequest.enabled() : true;
                image.path = imageRequest.content() != null ? imageRequest.content() : imageRequest.path();
                if (image.path == null) throw new BadRequestException("Imagem sem conteúdo");
                product.images.add(image);
            }
        }
        if (product.options == null) product.options = new ArrayList<>();
        if (request.options() != null) {
            product.options.clear();
            for (RequestProduct.OptionRequest optionRequest : request.options()) {
                EntityProductOption option = new EntityProductOption(); option.product = product;
                option.title = optionRequest.title(); option.values = optionRequest.values();
                option.radius = optionRequest.radius() != null ? optionRequest.radius() : 0;
                if (optionRequest.shape() != null) option.shape = EntityProductOption.Shape.valueOf(optionRequest.shape());
                if (optionRequest.type() != null) option.type = EntityProductOption.OptionType.valueOf(optionRequest.type());
                product.options.add(option);
            }
        }
    }

    private ProductResponse toResponse(EntityProduct p) {
        List<Integer> categoryIds = p.categories == null ? List.of() : p.categories.stream().filter(Objects::nonNull).map(c -> c.id).toList();
        List<ProductResponse.ImageResponse> images = p.images == null ? List.of() : p.images.stream()
                .filter(Objects::nonNull).map(i -> new ProductResponse.ImageResponse(i.id, i.path)).toList();
        List<ProductResponse.OptionResponse> options = p.options == null ? List.of() : p.options.stream()
                .filter(Objects::nonNull).map(o -> new ProductResponse.OptionResponse(o.id, o.title,
                        o.shape == null ? null : o.shape.name(), o.radius,
                        o.type == null ? null : o.type.name(), o.values)).toList();
        return new ProductResponse(p.id, p.enabled, p.name, p.slug, p.stock, p.description,
                p.price, p.priceWithDiscount, categoryIds, images, options);
    }
}
