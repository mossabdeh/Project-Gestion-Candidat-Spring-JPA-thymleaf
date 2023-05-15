package ntic.tlsi.gestiondoctorat2.repo;

import jakarta.persistence.Id;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.entities.VD;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatRepo extends UserRepo{
    List<Candidat> findAllBy();

    Page<Candidat> findByNomContains(String kw, Pageable pageable);



    Candidat findCandidatById(Long id);


    Optional<CandidatDTO> findDTOByUsername(String username);
}
