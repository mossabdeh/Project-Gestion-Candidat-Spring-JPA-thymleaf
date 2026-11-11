package ntic.tlsi.gestiondoctorat2.entities.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.entities.User;

@Data
public class UserDTO {
    private Long id;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String email;
    private String nom;
    private String prenom;
    @Enumerated(EnumType.STRING)
    private Role typeRole;

    public static UserDTO from(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());

        userDTO.setEmail(user.getEmail());
        userDTO.setNom(user.getNom());
        userDTO.setPrenom(user.getPrenom());
        userDTO.setTypeRole(user.getTypeRole());
        return userDTO;
    }

}
