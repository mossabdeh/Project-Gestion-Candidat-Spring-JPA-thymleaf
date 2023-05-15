package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.DTO.VdDTO;
import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.entities.VD;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VDRepo extends UserRepo{

    VD findByNom(String vd);


    Optional<VdDTO> findDTOByUsername(String username);
}
