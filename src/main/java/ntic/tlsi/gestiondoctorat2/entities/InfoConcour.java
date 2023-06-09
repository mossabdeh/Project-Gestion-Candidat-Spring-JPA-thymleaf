package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Data
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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date DateConcour;
    // No place because it happen only in one departelent NTIC
    @ManyToOne
    private VD vd ;

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
    public boolean isMatier1DifferentFromMatier2() {
        return Matier1 != Matier2;
    }
}
