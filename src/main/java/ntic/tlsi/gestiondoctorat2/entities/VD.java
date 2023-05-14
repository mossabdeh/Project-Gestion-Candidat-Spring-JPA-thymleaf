package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.VdDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Data
public class VD extends User{
     private Date LogDate;
     @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.LAZY)
     // if VD deleted every Concour is deleted
     // if you want delete VD alone u should replcae cascade = CascadeType.ALL with cascade = CascadeType.REMOVE
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


    public void setConcours(Collection<InfoConcour> concours) {
        this.concours = concours;
    }
    public static VD from(VdDTO vdDTO){
        VD vd =new VD();
        vd.setLogDate(vdDTO.getLogDate());
        vd.setUsername(vdDTO.getUsername());
        vd.setPassword(vdDTO.getPassword());
        vd.setEmail(vdDTO.getEmail());
        vd.setNom(vdDTO.getNom());
        vd.setPrenom(vdDTO.getPrenom());
        vd.setTypeRole(Role.VD);
        return vd;

}}
