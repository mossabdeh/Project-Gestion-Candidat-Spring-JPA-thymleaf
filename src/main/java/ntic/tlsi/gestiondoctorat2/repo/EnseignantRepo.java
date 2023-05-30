package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.DTO.EnseignantDTO;
import ntic.tlsi.gestiondoctorat2.entities.Enseignant;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import ntic.tlsi.gestiondoctorat2.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnseignantRepo extends UserRepo{
    List<Enseignant> findAllBy();

    List<Enseignant> findAllBySpecialite(Matier matier);

    Page<Enseignant> findByNomContains(String keyword, Pageable pageable);


    Enseignant findEnseignantById(Long id);


    Optional<EnseignantDTO> findDTOByUsername(String username);

    List<Enseignant> findBySpecialite(Matier matier);
}
