package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.entities.VD;
import org.springframework.stereotype.Repository;

@Repository
public interface VDRepo extends UserRepo{

    VD findByNom(String vd);

    User findByUsername(String username);
}
