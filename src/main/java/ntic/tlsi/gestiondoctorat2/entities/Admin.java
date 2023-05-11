package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;

import java.util.Date;

@Entity
@Data
public class Admin extends User{
    @Temporal(TemporalType.DATE)
    private Date LogDate;

    public Admin() {
    }

    public Admin(String username, String password, String email, String nom, String prenom, Role typeRole, Date logDate) {
        super(username, password, email, nom, prenom, typeRole);
        LogDate = logDate;
    }

    public static Admin from(AdminDTO adminDTO){
        Admin admin =new Admin();
        admin.setLogDate(adminDTO.getLogDate());
        admin.setUsername(adminDTO.getUsername());
        admin.setPassword(adminDTO.getPassword());
        admin.setEmail(adminDTO.getEmail());
        admin.setNom(adminDTO.getNom());
        admin.setPrenom(adminDTO.getPrenom());
        admin.setTypeRole(Role.ADMIN);
        return admin;

    }
}
