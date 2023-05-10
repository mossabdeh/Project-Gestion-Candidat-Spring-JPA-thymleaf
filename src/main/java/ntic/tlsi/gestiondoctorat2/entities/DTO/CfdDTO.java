package ntic.tlsi.gestiondoctorat2.entities.DTO;

import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.CFD;
import ntic.tlsi.gestiondoctorat2.entities.Role;

import java.util.Date;

@Data
public class CfdDTO extends UserDTO{
    private Date LogDate;

    public static CfdDTO from(CFD cfd){
        CfdDTO cfdDTO = new CfdDTO();
        cfdDTO.setId(cfd.getId());
        cfdDTO.setLogDate(cfd.getLogDate());
        cfdDTO.setUsername(cfd.getUsername());
        cfdDTO.setPassword(cfd.getPassword());
        cfdDTO.setEmail(cfd.getEmail());
        cfdDTO.setNom(cfd.getNom());
        cfdDTO.setPrenom(cfd.getPrenom());
        cfdDTO.setTypeRole(Role.CFD);
        return cfdDTO;
    }
}
