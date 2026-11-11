package ntic.tlsi.gestiondoctorat2.web;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.UserAccountRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Updates an existing managed Candidate in one transaction.
 * Never reconstruct a partially populated JPA entity from a web form.
 */
@Service
public class CandidatProfileService {
    private final CandidatRepo candidatRepo;
    private final UserAccountRepo userAccountRepo;

    public CandidatProfileService(CandidatRepo candidatRepo, UserAccountRepo userAccountRepo) {
        this.candidatRepo = candidatRepo;
        this.userAccountRepo = userAccountRepo;
    }

    @Transactional
    public void updateProfile(CandidatProfileForm profile) {
        Candidat candidate = candidatRepo.findCandidatOptionalById(profile.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Candidate not found"));

        String username = profile.getUsername().trim();
        if (userAccountRepo.existsByUsernameAndIdNot(username, candidate.getId())) {
            throw new UsernameAlreadyUsedException();
        }

        candidate.setUsername(username);
        candidate.setNom(profile.getNom().trim());
        candidate.setPrenom(profile.getPrenom().trim());
        candidate.setEmail(profile.getEmail().trim());
        candidate.setDateNaissance(profile.getDateNaissance());
        // Managed entity dirty checking persists these five allowlisted fields.
        // All other identity, grade, admission and paper fields remain intact.
    }

    public static class UsernameAlreadyUsedException extends RuntimeException {
    }
}
