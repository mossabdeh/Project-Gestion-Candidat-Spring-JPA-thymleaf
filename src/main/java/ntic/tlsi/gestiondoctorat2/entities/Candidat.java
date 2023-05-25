package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Data
@DynamicUpdate
public class Candidat extends User{
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;

    private String code;

    private double moyMatier1;
    private double moyMatier2;

    private boolean getPoste;

    private double moyenneGeneral;

    @OneToMany(mappedBy = "candidat",cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.LAZY)
    //@Size(max =2, message = "The maximum size of the copies is 2 , one for every matier")
    private Collection<Copie> copies = new ArrayList<>();

    public Candidat() {
    }

    public Candidat(String username, String password, String email, String nom, String prenom, Role typeRole, Date dateNaissance, String code, double moyenneGeneral) {
        super(username, password, email, nom, prenom, typeRole);
        this.dateNaissance = dateNaissance;
        this.code = code;
        this.moyenneGeneral = moyenneGeneral;
    }



    // The maximum size of the copies is 2, one for every matier
    public void setCopies(Collection<Copie> copies) {
        if (copies.size() <= 2) {
            this.copies = copies;
        } else {
            throw new IllegalArgumentException("The maximum size of the copies is 2, one for every matier");
        }
    }
    @Transient
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


     public static Candidat from(CandidatDTO candidatDTO){
         Candidat candidat =new Candidat();
         candidat.setDateNaissance(candidatDTO.getDateNaissance());
         candidat.setCode(candidatDTO.getCode());
         candidat.setMoyenneGeneral(candidatDTO.getMoyenneGeneral());
         candidat.setMoyMatier1(candidatDTO.getMoyMatier1());
         candidat.setMoyMatier2(candidatDTO.getMoyMatier2());
         candidat.setUsername(candidatDTO.getUsername());
         candidat.setPassword(passwordEncoder.encode(candidatDTO.getPassword())); // Hash the password
         candidat.setEmail(candidatDTO.getEmail());
         candidat.setNom(candidatDTO.getNom());
         candidat.setPrenom(candidatDTO.getPrenom());
         candidat.setTypeRole(Role.CANDIDAT);
         return candidat;
     }

}
