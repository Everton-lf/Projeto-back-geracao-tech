package tech.geracao.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import tech.geracao.model.EntityUsuarios;

import java.util.List;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<EntityUsuarios, Integer> {
    public boolean existePorEmail(String email) {
        return count("email", email) > 0;
    }

}
