package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "USERS")
public abstract class User {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String nom;
    private String prenom;
    @Enumerated(EnumType.STRING)
    private Role typeRole;

    public User() {
    }

    public User(String username, String password, String email, String nom, String prenom, Role typeRole) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.typeRole = typeRole;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public Role getTypeRole() {
        return typeRole;
    }
}
