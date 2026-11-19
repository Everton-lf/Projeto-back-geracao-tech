package tech.geracao.model;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

// EntityCategory — Representa uma categoria de produtos exibível no menu ou em filtros.
@Entity
@Table(name = "category")
public class EntityCategory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String slug;

    @Column(name = "use_in_menu")
    public Boolean useInMenu = false;

    @ManyToMany(mappedBy = "categories")
    public List<EntityProduct> products;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;
}