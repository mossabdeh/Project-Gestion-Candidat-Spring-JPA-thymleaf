package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Data
public class Candidat extends User{
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    private long code;

    private double moyMatier1;
    private double moyMatier2;

    private boolean getPoste;

    private double moyenneGeneral;

    @OneToMany(mappedBy = "candidat",cascade = CascadeType.ALL,orphanRemoval = true)
    //@Size(max =2, message = "The maximum size of the copies is 2 , one for every matier")
    private Collection<Copie> copies = new ArrayList<>();

    public Candidat() {
    }

    public Candidat(String username, String password, String email, String nom, String prenom, Role typeRole, Date dateNaissance, long code, double moyenneGeneral) {
        super(username, password, email, nom, prenom, typeRole);
        this.dateNaissance = dateNaissance;
        this.code = code;
        this.moyenneGeneral = moyenneGeneral;
    }



    // The maximum size of the copies is 2, one for every matier
    public void setCopies(Collection<Copie> copies) {
        if (copies.size() <= 2) {
            this.copies = copies;
        } else {
            throw new IllegalArgumentException("The maximum size of the copies is 2, one for every matier");
        }
    }
}
