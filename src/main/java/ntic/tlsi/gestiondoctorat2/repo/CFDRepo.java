package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.CFD;
import ntic.tlsi.gestiondoctorat2.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public interface CFDRepo extends UserRepo{

    Page<CFD> findByNomContains(String keyword, PageRequest of);

    boolean existsByUsernameAndIdNot(String username, Long id);
}
