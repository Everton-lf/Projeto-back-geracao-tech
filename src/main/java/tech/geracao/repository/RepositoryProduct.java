package tech.geracao.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import tech.geracao.model.EntityProduct;

// RepositoryProduct — Repositório Panache utilizado para persistência e consultas de produtos.
@ApplicationScoped
public class RepositoryProduct implements PanacheRepositoryBase<EntityProduct, Integer> {
}
