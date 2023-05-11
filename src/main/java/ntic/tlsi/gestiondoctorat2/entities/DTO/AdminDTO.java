package ntic.tlsi.gestiondoctorat2.entities.DTO;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Role;

import java.util.Date;

@Data
public class AdminDTO extends UserDTO{
    @Temporal(TemporalType.DATE)
    private Date LogDate;

    public static AdminDTO from(Admin admin){
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setId(admin.getId());
        adminDTO.setLogDate(admin.getLogDate());
        adminDTO.setUsername(admin.getUsername());
        adminDTO.setPassword(admin.getPassword());
        adminDTO.setEmail(admin.getEmail());
        adminDTO.setNom(admin.getNom());
        adminDTO.setPrenom(admin.getPrenom());
        adminDTO.setTypeRole(Role.ADMIN);
        return adminDTO;
    }
}
