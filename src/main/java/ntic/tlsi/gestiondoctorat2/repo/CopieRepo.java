package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.Copie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CopieRepo extends JpaRepository<Copie,Long> {
    List<Copie> findByCandidatId(Long candidateId);

}
