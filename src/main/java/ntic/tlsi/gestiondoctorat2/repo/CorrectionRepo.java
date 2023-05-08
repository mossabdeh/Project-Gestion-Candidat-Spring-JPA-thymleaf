package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.Correction;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrectionRepo extends JpaRepository<Correction,Long> {
}
