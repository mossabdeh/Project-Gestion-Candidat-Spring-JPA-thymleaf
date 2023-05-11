package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends UserRepo{
    Admin findAdminById(Long id);

    Page<Admin> findByNomContains(String keyword, PageRequest of);
}
