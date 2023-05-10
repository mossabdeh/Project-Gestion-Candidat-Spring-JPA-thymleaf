package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
public class Copie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @Enumerated(EnumType.STRING)
    private Matier matier;
    @ManyToOne// bidirictional relation with candidat
    @JoinColumn(name = "Candidat_id")
    private Candidat candidat;

    @ManyToMany(mappedBy = "CorrectionCopies",cascade = CascadeType.ALL)
    private Collection<Enseignant>  enseignantsAtt = new ArrayList<>();

    @OneToMany(mappedBy = "copie")
    private List<Correction> corrections = new ArrayList<>();


    public Copie() {
    }


    // the constructor should contain an candidat :D
    public Copie(Matier matier, Candidat candidat) {
        this.matier = matier;
        this.candidat = candidat;
    }

    // methode for enseignant
    /*public  void CorrectedBy(Enseignant enseignant){
        enseignantsAtt.add(enseignant);
    }*/



    public Matier getMatier() {
        return matier;
    }

    public Candidat getCandidat() {
        return candidat;
    }
}
