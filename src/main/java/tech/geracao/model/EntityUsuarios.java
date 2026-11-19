package tech.geracao.model;


import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.mutiny.core.eventbus.Message;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;


// EntityUsuarios — Representa a entidade de usuário e a tabela users no banco de dados.
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class EntityUsuarios extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    @NotNull(message = "O nome é obrigatório")
    private String firstname;

    @Column(nullable = false, length = 50)
    @NotNull(message = "O sobrenome é obrigatório")
    private String surname;

    @Column(nullable = false, unique = true, length = 100)
    @Email(message = "O email deve ser válido")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "A senha é obrigatória")
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Aplica o hash BCrypt antes que a senha seja armazenada.
    public void criptografarSenha() {
        if (this.password != null) {
            this.password = BcryptUtil.bcryptHash(this.password);
        }
    }
}