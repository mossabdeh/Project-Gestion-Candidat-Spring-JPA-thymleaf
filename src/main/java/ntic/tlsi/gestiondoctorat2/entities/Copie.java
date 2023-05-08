package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;

@Entity
public class Copie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @Enumerated(EnumType.STRING)
    private Matier matier;
    @ManyToOne// bidirictional relation with candidat
    @JoinColumn(name = "Candidat_id")
    private Candidat candidat;

    public Copie() {
    }


    // the constructor should contain an candidat :D
    public Copie(Matier matier, Candidat candidat) {
        this.matier = matier;
        this.candidat = candidat;
    }

    public Matier getMatier() {
        return matier;
    }

    public Candidat getCandidat() {
        return candidat;
    }
}
