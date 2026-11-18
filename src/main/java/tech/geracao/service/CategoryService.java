package tech.geracao.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import tech.geracao.model.EntityCategory;
import tech.geracao.repository.RepositoryCategory;
import tech.geracao.view.dtoResponse.CategoryResponse;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CategoryService {

    @Inject
    RepositoryCategory categoryRepository;

    @Transactional
    public List<CategoryResponse> listarCategorias(Integer limit, Integer page, Boolean useInMenu) {
        PanacheQuery<EntityCategory> query;

        if (useInMenu == null) {
            query = EntityCategory.findAll(Sort.by("name").ascending());
        } else {
            query = EntityCategory.find("useInMenu = ?1", Sort.by("name").ascending(), useInMenu);
        }

        if (limit != null && limit >= 0) {
            query.page(Page.of(Math.max(page - 1, 0), limit));
        }

        return query.list().stream()
                .map(category -> new CategoryResponse(
                        category.id,
                        category.name,
                        category.slug,
                        category.useInMenu
                ))
                .toList();
    }

    public long contarCategorias(Boolean useInMenu) {
        if (useInMenu == null) {
            return EntityCategory.count();
        }

        return EntityCategory.count("useInMenu = ?1", useInMenu);
    }

    @Transactional
    public CategoryResponse criarCategoria(CategoryResponse categoryResponse) {
        EntityCategory novaCategoria = new EntityCategory();
        novaCategoria.name = categoryResponse.name();
        novaCategoria.slug = categoryResponse.slug();
        novaCategoria.useInMenu = categoryResponse.useInMenu();

        categoryRepository.persist(novaCategoria);

        return new CategoryResponse(
                novaCategoria.id,
                novaCategoria.name,
                novaCategoria.slug,
                novaCategoria.useInMenu
        );
    }
    public CategoryResponse buscarCategoriaPorId(Integer id) {
        EntityCategory categoria = categoryRepository.findById(id);
        if (categoria == null) {
            throw new NotFoundException("Categoria não encontrada");
        }

        return new CategoryResponse(
                categoria.id,
                categoria.name,
                categoria.slug,
                categoria.useInMenu
        );
    }

    @Transactional
    public void deletarCategoria(Integer id) {
        EntityCategory categoria = categoryRepository.findById(id);
        if (categoria == null) {
            throw new NotFoundException("Categoria não encontrada");
        }

        categoryRepository.delete(categoria);
    }

    @Transactional
    public CategoryResponse editarCategoria(Integer id, CategoryResponse categoryResponse) {
        EntityCategory categoria = categoryRepository.findById(id);
        if (categoria == null) {
            throw new NotFoundException("Categoria não encontrada");
        }

        categoria.name = categoryResponse.name();
        categoria.slug = categoryResponse.slug();
        categoria.useInMenu = categoryResponse.useInMenu();
        categoria.updatedAt = LocalDateTime.now();

        return new CategoryResponse(
                categoria.id,
                categoria.name,
                categoria.slug,
                categoria.useInMenu

        );
    }
}
