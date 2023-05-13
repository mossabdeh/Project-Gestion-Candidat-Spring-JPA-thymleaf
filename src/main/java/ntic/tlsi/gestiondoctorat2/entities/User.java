package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import lombok.Data;
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
    private String password;
    private String email;
    private String nom;
    private String prenom;
    @Enumerated(EnumType.STRING)
    private Role typeRole;

    public User() {
    }
    @PrePersist
    @PreUpdate
    private void hashPassword() {
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
