package ntic.tlsi.gestiondoctorat2.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ntic.tlsi.gestiondoctorat2.entities.Enseignant;
import ntic.tlsi.gestiondoctorat2.entities.Matier;

/** Teacher-only editable attributes. Existing correction assignments are never bound. */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherProfileForm extends StaffProfileForm {
    @NotBlank
    private String grade;
    @NotNull
    private Matier specialite;

    public static TeacherProfileForm from(Enseignant existing) {
        TeacherProfileForm form = new TeacherProfileForm();
        form.setId(existing.getId());
        form.setUsername(existing.getUsername());
        form.setNom(existing.getNom());
        form.setPrenom(existing.getPrenom());
        form.setEmail(existing.getEmail());
        form.setGrade(existing.getGrade());
        form.setSpecialite(existing.getSpecialite());
        return form;
    }
}
