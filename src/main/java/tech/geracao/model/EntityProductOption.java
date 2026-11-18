package tech.geracao.model;



import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_options")
public class EntityProductOption extends PanacheEntityBase {

    public enum Shape {
        square, circle
    }

    public enum OptionType {
        text, color
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public EntityProduct product;

    @Column(nullable = false)
    public String title;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('square', 'circle')")
    public Shape shape = Shape.square;

    public Integer radius = 0;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('text', 'color')")
    public OptionType type = OptionType.text;

    @Column(name = "`values`", nullable = false)
    public String values;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;
}
