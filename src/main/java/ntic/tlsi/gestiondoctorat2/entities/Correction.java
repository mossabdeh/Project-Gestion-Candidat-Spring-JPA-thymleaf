package ntic.tlsi.gestiondoctorat2.entities;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
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

    // Null distinguishes an unmarked paper from the valid grade zero.
    @Column(nullable = true)
    private Double note;

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public void setCopie(Copie copie) {
        this.copie = copie;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public Copie getCopie() {
        return copie;
    }

    public Double getNote() {
        return note;
    }

    // getters and setters
}

