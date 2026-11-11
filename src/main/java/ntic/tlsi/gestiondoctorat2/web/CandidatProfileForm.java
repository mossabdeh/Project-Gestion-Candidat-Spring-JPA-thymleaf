package ntic.tlsi.gestiondoctorat2.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * The only fields accepted from the Admin Candidate profile-edit form.
 * Exam codes, scores, admissions, paper associations, passwords and roles
 * are intentionally not bindable through this object.
 */
@Data
public class CandidatProfileForm {

    @NotNull
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username is too long")
    private String username;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name is too long")
    private String nom;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name is too long")
    private String prenom;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 254, message = "Email is too long")
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateNaissance;

    public static CandidatProfileForm from(Candidat existing) {
        CandidatProfileForm form = new CandidatProfileForm();
        form.setId(existing.getId());
        form.setUsername(existing.getUsername());
        form.setNom(existing.getNom());
        form.setPrenom(existing.getPrenom());
        form.setEmail(existing.getEmail());
        form.setDateNaissance(existing.getDateNaissance());
        return form;
    }
}
