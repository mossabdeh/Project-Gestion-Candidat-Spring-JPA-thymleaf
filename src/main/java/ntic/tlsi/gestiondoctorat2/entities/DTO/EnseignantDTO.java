package ntic.tlsi.gestiondoctorat2.entities.DTO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.Enseignant;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import ntic.tlsi.gestiondoctorat2.entities.Role;

import java.util.Date;

@Data
public class EnseignantDTO extends UserDTO{
    private String grade ;
    @Enumerated(EnumType.STRING)
    private Matier specialite;
    public static EnseignantDTO from(Enseignant enseignant){
        EnseignantDTO enseignantDTO = new EnseignantDTO();

        enseignantDTO.setId(enseignant.getId());
        enseignantDTO.setSpecialite(enseignant.getSpecialite());
        enseignantDTO.setGrade(enseignant.getGrade());
        enseignantDTO.setUsername(enseignant.getUsername());
        enseignantDTO.setPassword(enseignant.getPassword());
        enseignantDTO.setEmail(enseignant.getEmail());
        enseignantDTO.setNom(enseignant.getNom());
        enseignantDTO.setPrenom(enseignant.getPrenom());
        enseignantDTO.setTypeRole(Role.ENSEIGNANT);
        return enseignantDTO;
    }
}
