package ntic.tlsi.gestiondoctorat2.entities;


import jakarta.persistence.*;

@Entity
@Table(name = "Correction")
public class Correction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    @ManyToOne
    @JoinColumn(name = "copie_id")
    private Copie copie;

    private double note;

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public void setCopie(Copie copie) {
        this.copie = copie;
    }

    public void setNote(double note) {
        this.note = note;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public Copie getCopie() {
        return copie;
    }

    public double getNote() {
        return note;
    }

    // getters and setters
}

