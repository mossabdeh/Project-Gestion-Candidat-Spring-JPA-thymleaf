package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.EnseignantDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
public class Enseignant extends User{
    private String grade ;
    @Enumerated(EnumType.STRING)
    private Matier specialite;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable( name = "Correction",
            joinColumns = @JoinColumn(name = "Enseignant_id"),
            inverseJoinColumns = @JoinColumn(name = "Copie_id")
    )
    private Collection<Copie> CorrectionCopies = new ArrayList<>();

    @OneToMany(mappedBy = "enseignant")
    private List<Correction> corrections = new ArrayList<>();
    public Enseignant() {
    }

    public Enseignant(String username, String password, String email, String nom, String prenom, Role typeRole, String grade, Matier specialite) {
        super(username, password, email, nom, prenom, typeRole);
        this.grade = grade;
        this.specialite = specialite;
    }

    public Matier getSpecialite() {
        return specialite;
    }

    public static Enseignant from(EnseignantDTO enseignantDTO){
        Enseignant enseignant =new Enseignant();
        enseignant.setGrade(enseignantDTO.getGrade());
        enseignant.setSpecialite(enseignantDTO.getSpecialite());
        enseignant.setUsername(enseignantDTO.getUsername());
        enseignant.setPassword(enseignantDTO.getPassword());
        enseignant.setEmail(enseignantDTO.getEmail());
        enseignant.setNom(enseignantDTO.getNom());
        enseignant.setPrenom(enseignantDTO.getPrenom());
        enseignant.setTypeRole(Role.ENSEIGNANT);
        return enseignant;
}}
