package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity

public class VD extends User{
     private Date LogDate;
     @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true) // if VD deleted every Concour is deleted
     @JoinColumn(name = "VD_id")
     private Collection<InfoConcour> concours = new ArrayList<>();

    public VD() {
    }

    public VD(String username, String password, String email, String nom, String prenom, Role typeRole, Date logDate) {
        super(username, password, email, nom, prenom, typeRole);
        LogDate = logDate;
    }

    public VD(String username, String password, String email, String nom, String prenom, Role typeRole, Date logDate, Collection<InfoConcour> concours) {
        super(username, password, email, nom, prenom, typeRole);
        LogDate = logDate;
        this.concours = concours;
    }

    public Collection<InfoConcour> getConcours() {
        return concours;
    }
// Setters
    public void setLogDate(Date logDate) {
        LogDate = logDate;
    }

    public void setConcours(Collection<InfoConcour> concours) {
        this.concours = concours;
    }
}
