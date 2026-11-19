package tech.geracao.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import tech.geracao.model.EntityCategory;

// RepositoryCategory — Repositório Panache utilizado para persistência e consultas de categorias.
@ApplicationScoped
public class RepositoryCategory implements PanacheRepositoryBase<EntityCategory, Integer> {
}
