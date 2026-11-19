package tech.geracao.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import tech.geracao.model.EntityUsuarios;

import java.util.List;

// UsuarioRepository — Repositório de usuários com operações específicas da entidade.
@ApplicationScoped
public class UsuarioRepository implements PanacheRepositoryBase<EntityUsuarios, Integer> {
    // Verifica se já existe um usuário com este endereço de e-mail.
    public boolean existePorEmail(String email) {
        return count("email", email) > 0;
    }

    public boolean existePorEmailExcetoId(String email, Integer id) {
        return count("email = ?1 and id <> ?2", email, id) > 0;
    }

}
