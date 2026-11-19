package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
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
    private java.time.Instant resultsPublishedAt;

    // Null remains supported for competitions created before the lifecycle migration.
    @Enumerated(EnumType.STRING)
    private CompetitionPhase phase;

    public CompetitionPhase effectivePhase() {
        if (resultsPublishedAt != null) return CompetitionPhase.PUBLISHED;
        return phase == null ? CompetitionPhase.REGISTRATION : phase;
    }

    /** Only forward transitions are allowed. Lock the competition row before calling. */
    public void advanceTo(CompetitionPhase next) {
        if (next.ordinal() < effectivePhase().ordinal()) {
            throw new IllegalStateException("Competition cannot return to an earlier phase");
        }
        this.phase = next;
    }

    // Local competition time in Europe/Paris. Opens inclusive; closes exclusive.
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationOpensAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationClosesAt;

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

    @AssertTrue(message = "Registration must open before it closes, no later than the exam day")
    public boolean isRegistrationWindowValid() {
        if (registrationOpensAt == null && registrationClosesAt == null) return true; // legacy records
        if (registrationOpensAt == null || registrationClosesAt == null || DateConcour == null
                || !registrationOpensAt.isBefore(registrationClosesAt)) return false;
        // Hibernate may hydrate DATE columns as java.sql.Date, whose toInstant() throws.
        java.time.LocalDate examDay = DateConcour instanceof java.sql.Date sqlDate
                ? sqlDate.toLocalDate()
                : DateConcour.toInstant().atZone(java.time.ZoneId.of("Europe/Paris")).toLocalDate();
        return !registrationClosesAt.toLocalDate().isAfter(examDay);
    }

    @AssertTrue(message = "matier1 and matier2 should have different values")
    public boolean isMatier1DifferentFromMatier2() {
        return Matier1 != Matier2;
    }
}
