package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.Enseignant;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnseignantRepo extends UserRepo{
    List<Enseignant> findAllBy();

    List<Enseignant> findAllBySpecialite(Matier matier);
}
