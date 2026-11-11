package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.Correction;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrectionRepo extends JpaRepository<Correction,Long> {
    List<Correction> findByCopieId(Long copyId);
    Correction findByIdAndEnseignantId(Long correctionId, Long loggedInEnseignantId);

    Page<Correction> findByEnseignantId(Long loggedInEnseignantId, PageRequest of);
}
