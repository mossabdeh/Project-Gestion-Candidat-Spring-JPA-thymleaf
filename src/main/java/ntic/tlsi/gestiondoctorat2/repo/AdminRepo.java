package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends UserRepo{
    Admin findAdminById(Long id);
}
