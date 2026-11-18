package ntic.tlsi.gestiondoctorat2.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.User;

/** Allowlisted identity fields for Admin editing Teacher, CFD and Vice Dean accounts. */
@Data
public class StaffProfileForm {
    @NotNull
    private Long id;
    @NotBlank @Size(max = 100)
    private String username;
    @NotBlank @Size(max = 100)
    private String nom;
    @NotBlank @Size(max = 100)
    private String prenom;
    @NotBlank @Email @Size(max = 254)
    private String email;

    public static StaffProfileForm from(User account) {
        StaffProfileForm form = new StaffProfileForm();
        form.setId(account.getId());
        form.setUsername(account.getUsername());
        form.setNom(account.getNom());
        form.setPrenom(account.getPrenom());
        form.setEmail(account.getEmail());
        return form;
    }
}
