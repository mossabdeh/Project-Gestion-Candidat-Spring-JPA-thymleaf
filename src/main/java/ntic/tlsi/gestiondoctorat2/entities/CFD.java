package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CfdDTO;

import java.util.Date;

@Entity
@Data
public class CFD extends User{
    private Date LogDate;

    public CFD() {
    }

    public CFD(String username, String password, String email, String nom, String prenom, Role typeRole, Date logDate) {
        super(username, password, email, nom, prenom, typeRole);
        LogDate = logDate;
    }

    public static CFD from(CfdDTO cfdDTO){
        CFD cfd =new CFD();
        cfd.setLogDate(cfdDTO.getLogDate());
        cfd.setUsername(cfdDTO.getUsername());
        cfd.setPassword(cfdDTO.getPassword());
        cfd.setEmail(cfdDTO.getEmail());
        cfd.setNom(cfdDTO.getNom());
        cfd.setPrenom(cfdDTO.getPrenom());
        cfd.setTypeRole(Role.CFD);
        return cfd;
}}
