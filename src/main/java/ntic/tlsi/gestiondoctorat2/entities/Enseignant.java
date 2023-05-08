package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity

public class Enseignant extends User{
    private String grade ;
    @Enumerated(EnumType.STRING)
    private Matier specialite;

    public Enseignant() {
    }

    public Enseignant(String username, String password, String email, String nom, String prenom, Role typeRole, String grade, Matier specialite) {
        super(username, password, email, nom, prenom, typeRole);
        this.grade = grade;
        this.specialite = specialite;
    }
}
