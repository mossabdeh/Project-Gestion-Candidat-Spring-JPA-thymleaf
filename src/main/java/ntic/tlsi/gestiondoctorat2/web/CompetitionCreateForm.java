package ntic.tlsi.gestiondoctorat2.web;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Only competition-creation fields may be supplied by a Vice Dean. */
@Data
public class CompetitionCreateForm {
    @NotNull @Min(1) @Max(3)
    private Integer nbrPostes;
    @NotBlank @Size(max = 255)
    private String specialite;
    @NotNull
    private Matier matier1;
    @NotNull
    private Matier matier2;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateConcour;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationOpensAt;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationClosesAt;

    @AssertTrue(message = "Competition subjects must be different")
    public boolean isSubjectsDifferent() {
        return matier1 == null || matier2 == null || matier1 != matier2;
    }

    @AssertTrue(message = "Registration must open before it closes, no later than exam day")
    public boolean isRegistrationWindowValid() {
        if (registrationOpensAt == null || registrationClosesAt == null || dateConcour == null) return true;
        LocalDate examDay = dateConcour instanceof java.sql.Date sqlDate
                ? sqlDate.toLocalDate()
                : dateConcour.toInstant().atZone(ZoneId.of("Europe/Paris")).toLocalDate();
        return registrationOpensAt.isBefore(registrationClosesAt)
                && !registrationClosesAt.toLocalDate().isAfter(examDay);
    }

    public InfoConcour toCompetition() {
        InfoConcour competition = new InfoConcour(nbrPostes, specialite.trim(),
                matier1, matier2, dateConcour);
        competition.setRegistrationOpensAt(registrationOpensAt);
        competition.setRegistrationClosesAt(registrationClosesAt);
        competition.setPhase(ntic.tlsi.gestiondoctorat2.entities.CompetitionPhase.REGISTRATION);
        return competition;
    }
}
