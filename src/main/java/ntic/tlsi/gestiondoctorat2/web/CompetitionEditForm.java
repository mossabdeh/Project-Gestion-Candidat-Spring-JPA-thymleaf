package ntic.tlsi.gestiondoctorat2.web;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;

/** An allowlisted request for updating a competition before registration begins. */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionEditForm extends CompetitionCreateForm {
    @NotNull
    private Long id;

    public static CompetitionEditForm from(InfoConcour competition) {
        CompetitionEditForm form = new CompetitionEditForm();
        form.setId(competition.getId());
        form.setNbrPostes(competition.getNbrPostes());
        form.setSpecialite(competition.getSpecialite());
        form.setMatier1(competition.getMatier1());
        form.setMatier2(competition.getMatier2());
        form.setDateConcour(competition.getDateConcour());
        form.setRegistrationOpensAt(competition.getRegistrationOpensAt());
        form.setRegistrationClosesAt(competition.getRegistrationClosesAt());
        return form;
    }
}
