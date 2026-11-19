package tech.geracao.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import tech.geracao.model.EntityCategory;
import tech.geracao.model.EntityProduct;
import tech.geracao.repository.RepositoryCategory;
import tech.geracao.view.dtoResponse.CategoryResponse;

import java.time.LocalDateTime;
import java.util.List;

// CategoryService — Camada de negócio responsável pelas categorias e pela comunicação com o banco.
@ApplicationScoped
public class CategoryService {

    @Inject
    RepositoryCategory categoryRepository;

    @Transactional
    // Consulta categorias em ordem alfabética e aplica filtro ou paginação.
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

    // Conta categorias, opcionalmente restringindo pelo menu.
    public long contarCategorias(Boolean useInMenu) {
        if (useInMenu == null) {
            return EntityCategory.count();
        }

        return EntityCategory.count("useInMenu = ?1", useInMenu);
    }

    @Transactional
    // Cria e persiste uma nova categoria.
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
    // Busca uma categoria pelo ID ou informa que ela não existe.
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
    // Remove uma categoria existente.
    public void deletarCategoria(Integer id) {
        EntityCategory categoria = categoryRepository.findById(id);
        if (categoria == null) {
            throw new NotFoundException("Categoria não encontrada");
        }

        if (categoria.products != null) {
            for (EntityProduct product : List.copyOf(categoria.products)) {
                if (product.categories != null) {
                    product.categories.remove(categoria);
                }
            }
        }
        categoryRepository.delete(categoria);
    }

    @Transactional
    // Atualiza os dados da categoria e registra a alteração.
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
