package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.web.CompetitionEditForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Updates competition setup without reconstructing a persistent entity from user input. */
@Service
public class CompetitionSetupService {
    private final InfoConRepo competitions;
    private final CompetitionEnrollmentRepo enrollments;
    private final EnrollmentExamPaperRepo papers;
    private final VDRepo viceDeans;

    public CompetitionSetupService(InfoConRepo competitions,
                                   CompetitionEnrollmentRepo enrollments,
                                   EnrollmentExamPaperRepo papers, VDRepo viceDeans) {
        this.competitions = competitions;
        this.enrollments = enrollments;
        this.papers = papers;
        this.viceDeans = viceDeans;
    }

    @Transactional(readOnly = true)
    public CompetitionEditForm editForm(Long id, String username) {
        InfoConcour competition = competitions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        checkOwner(competition, username);
        checkEditable(competition);
        return CompetitionEditForm.from(competition);
    }

    @Transactional
    public void update(CompetitionEditForm form, String username) {
        InfoConcour competition = competitions.findLockedById(form.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        checkOwner(competition, username);
        checkEditable(competition);
        competition.setNbrPostes(form.getNbrPostes());
        competition.setSpecialite(form.getSpecialite().trim());
        competition.setMatier1(form.getMatier1());
        competition.setMatier2(form.getMatier2());
        competition.setDateConcour(form.getDateConcour());
        competition.setRegistrationOpensAt(form.getRegistrationOpensAt());
        competition.setRegistrationClosesAt(form.getRegistrationClosesAt());
        // The owning VD, phase, publication timestamp and IDs are never changed.
    }

    private void checkOwner(InfoConcour competition, String username) {
        VD owner = competition.getVd();
        if (owner == null || !owner.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the competition owner may edit it");
        }
    }

    private void checkEditable(InfoConcour competition) {
        if (competition.effectivePhase() != CompetitionPhase.REGISTRATION
                || enrollments.existsByCompetition_Id(competition.getId())
                || papers.existsByEnrollment_Competition_Id(competition.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Competition setup is locked after the first enrollment or preparation");
        }
    }
}
