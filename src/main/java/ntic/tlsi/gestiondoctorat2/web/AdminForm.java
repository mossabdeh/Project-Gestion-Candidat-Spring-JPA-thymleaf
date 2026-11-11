package ntic.tlsi.gestiondoctorat2.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Admin;

/**
 * Fields an administrator may submit in the create/edit forms.
 * Password is required and checked separately only when creating an account.
 */
@Data
public class AdminForm {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username is too long")
    private String username;

    private String password;

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

    public static AdminForm from(Admin admin) {
        AdminForm form = new AdminForm();
        form.setId(admin.getId());
        form.setUsername(admin.getUsername());
        form.setNom(admin.getNom());
        form.setPrenom(admin.getPrenom());
        form.setEmail(admin.getEmail());
        return form;
    }

    public Admin toAdmin() {
        Admin admin = new Admin();
        admin.setUsername(username == null ? null : username.trim());
        admin.setNom(nom == null ? null : nom.trim());
        admin.setPrenom(prenom == null ? null : prenom.trim());
        admin.setEmail(email == null ? null : email.trim());
        return admin;
    }
}
