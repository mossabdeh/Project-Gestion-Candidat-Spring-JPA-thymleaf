package ntic.tlsi.gestiondoctorat2.entities.DTO;

import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.entities.VD;

import java.util.Date;

@Data
public class VdDTO extends UserDTO{
    private Date LogDate;

    public static VdDTO from(VD vd){
        VdDTO vdDTO = new VdDTO();
        vdDTO.setId(vd.getId());
        vdDTO.setLogDate(vd.getLogDate());
        vdDTO.setUsername(vd.getUsername());
        vdDTO.setPassword(vd.getPassword());
        vdDTO.setEmail(vd.getEmail());
        vdDTO.setNom(vd.getNom());
        vdDTO.setPrenom(vd.getPrenom());
        vdDTO.setTypeRole(Role.VD);
        return vdDTO;
    }
}
