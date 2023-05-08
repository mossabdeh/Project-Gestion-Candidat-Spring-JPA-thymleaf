package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
public class InfoConcour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Max(value = 3, message = "The maximum number of the Postes is 3")
    private Integer NbrPostes; // nbrPostes availailbe for doctorat
    private String Specialite;
    @Enumerated(EnumType.STRING)
    private Matier Matier1;
    @Enumerated(EnumType.STRING)
    private Matier Matier2;

    @Temporal(TemporalType.DATE)
    private Date DateConcour;
    // No place because it happen only in one departelent NTIC


    public InfoConcour() {
    }


    public InfoConcour(Integer nbrPostes, String specialite, Matier matier1, Matier matier2, Date dateConcour) {
        NbrPostes = nbrPostes;
        Specialite = specialite;
        Matier1 = matier1;
        Matier2 = matier2;
        DateConcour = dateConcour;
    }

    @AssertTrue(message = "matier1 and matier2 should have different values")
    private boolean isMatier1DifferentFromMatier2() {
        return Matier1 != Matier2;
    }
}
