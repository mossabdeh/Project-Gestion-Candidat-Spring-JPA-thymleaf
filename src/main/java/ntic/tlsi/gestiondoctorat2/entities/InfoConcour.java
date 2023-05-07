package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
public class InfoConcour {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;


    @Max(3)
    private Integer NbrPostes; // nbrPostes availailbe for doctorat
    private String  Specialite;
    private String Matier1;
    private String Matier2;

    @Temporal(TemporalType.DATE)
    private Date DateConcour;
    // No place because it happen only in one departelent NTIC


    public InfoConcour() {
    }



    public InfoConcour(Integer nbrPostes, String specialite, String matier1, String matier2, Date dateConcour) {
        NbrPostes = nbrPostes;
        Specialite = specialite;
        Matier1 = matier1;
        Matier2 = matier2;
        DateConcour = dateConcour;
    }
}
