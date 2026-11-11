package ntic.tlsi.gestiondoctorat2.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import ntic.tlsi.gestiondoctorat2.entities.DTO.UserDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "USERS")
@Data
public abstract class User {
    @Transient
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;
    private String email;
    private String nom;
    private String prenom;
    @Enumerated(EnumType.STRING)
    private Role typeRole;

    public User() {
    }
    @PrePersist
    private void hashPassword() {
        if (this.password == null || this.password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        this.password = passwordEncoder.encode(this.password);
    }
    public User(String username, String password, String email, String nom, String prenom, Role typeRole) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.typeRole = typeRole;
    }

}
