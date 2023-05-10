package ntic.tlsi.gestiondoctorat2.entities.DTO;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.Role;

import java.util.Date;

@Data
public class CandidatDTO extends UserDTO{
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    private long code;

    private double moyMatier1;
    private double moyMatier2;

    private boolean getPoste;

    private double moyenneGeneral;

    public static CandidatDTO from(Candidat candidat){
        CandidatDTO candidatDTO = new CandidatDTO();
        candidatDTO.setId(candidat.getId());
        candidatDTO.setCode(candidat.getCode());
        candidatDTO.setMoyMatier1(candidat.getMoyMatier1());
        candidatDTO.setMoyMatier2(candidat.getMoyMatier2());
        candidatDTO.setMoyenneGeneral(candidat.getMoyenneGeneral());
        candidatDTO.setDateNaissance(candidat.getDateNaissance());
        candidatDTO.setGetPoste(candidat.isGetPoste());
        candidatDTO.setUsername(candidat.getUsername());
        candidatDTO.setPassword(candidat.getPassword());
        candidatDTO.setEmail(candidat.getEmail());
        candidatDTO.setNom(candidat.getNom());
        candidatDTO.setPrenom(candidat.getPrenom());
        candidatDTO.setTypeRole(Role.CANDIDAT);
        return candidatDTO;
    }
}
