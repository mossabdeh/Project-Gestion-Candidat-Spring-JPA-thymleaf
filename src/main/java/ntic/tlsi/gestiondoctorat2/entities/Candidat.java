package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity

public class Candidat extends User{
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;
    private long code;
    private double moyenneGeneral;

    public Candidat() {
    }

    public Candidat(String username, String password, String email, String nom, String prenom, Role typeRole, Date dateNaissance, long code, double moyenneGeneral) {
        super(username, password, email, nom, prenom, typeRole);
        this.dateNaissance = dateNaissance;
        this.code = code;
        this.moyenneGeneral = moyenneGeneral;
    }
}
