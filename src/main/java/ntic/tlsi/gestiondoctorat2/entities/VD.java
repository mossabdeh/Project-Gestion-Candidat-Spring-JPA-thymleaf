package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Date;

@Entity

public class VD extends User{
     private Date LogDate;

    public VD() {
    }

    public VD(String username, String password, String email, String nom, String prenom, Role typeRole, Date logDate) {
        super(username, password, email, nom, prenom, typeRole);
        LogDate = logDate;
    }
}
