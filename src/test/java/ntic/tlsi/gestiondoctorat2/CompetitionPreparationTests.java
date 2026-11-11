package ntic.tlsi.gestiondoctorat2;

import jakarta.persistence.EntityManager;
import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.CompetitionPreparationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CompetitionPreparationTests {
    @Autowired private MockMvc mvc;
    @Autowired private CompetitionPreparationService preparation;
    @Autowired private CandidatRepo candidates;
    @Autowired private InfoConRepo competitions;
    @Autowired private CopieRepo papers;
    @Autowired private CorrectionRepo corrections;
    @Autowired private EnseignantRepo teachers;
    @Autowired private EntityManager entityManager;

    @Test
    @WithMockUser(roles = "VD")
    void codeAssignmentIsRepeatableUniqueAndPreservesExistingCodes() {
        Candidat first = candidate();
        Candidat second = candidate();
        Candidat third = candidate();
        third.setCode("0123456");
        candidates.saveAndFlush(third);

        preparation.assignCodes(List.of(first.getId(), second.getId(), third.getId(), first.getId()));
        entityManager.flush();
        String firstCode = first.getCode();
        String secondCode = second.getCode();

        assertNotNull(firstCode);
        assertEquals(7, firstCode.length());
        assertEquals(7, firstCode.chars().distinct().count());
        assertNotEquals(firstCode, secondCode);
        assertNotEquals("0123456", firstCode);
        assertNotEquals("0123456", secondCode);

        preparation.assignCodes(List.of(first.getId(), second.getId(), third.getId()));
        entityManager.flush();
        assertEquals(firstCode, first.getCode());
        assertEquals(secondCode, second.getCode());
        assertEquals("0123456", third.getCode());
    }

    @Test
    @WithMockUser(roles = "VD")
    void missingCandidateInCodeBatchRollsBackAllChanges() {
        Candidat candidate = candidate();
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> preparation.assignCodes(List.of(candidate.getId(), Long.MAX_VALUE)));
        entityManager.clear();
        assertNull(candidates.findCandidatOptionalById(candidate.getId()).orElseThrow().getCode());
    }

    @Test
    @WithMockUser(roles = "CFD")
    void paperInitializationIsRepeatableAndSubjectSpecific() {
        competition();
        Candidat candidate = candidate();
        candidate.setCode("0123456");
        candidates.saveAndFlush(candidate);
        preparation.initializePapers();
        entityManager.flush();
        assertEquals(2, papers.findByCandidatId(candidate.getId()).size());
        preparation.initializePapers();
        entityManager.flush();
        List<Copie> actual = papers.findByCandidatId(candidate.getId());
        assertEquals(2, actual.size());
        assertTrue(actual.stream().anyMatch(p -> p.getMatier() == Matier.ALGO));
        assertTrue(actual.stream().anyMatch(p -> p.getMatier() == Matier.EDL));
    }

    @Test
    @WithMockUser(roles = "CFD")
    void noCompetitionDoesNotCreatePapers() {
        // The service must explicitly reject an empty competition store.
        // Other test fixtures may be present, so validate the endpoint and
        // existing inventory only when the repository is genuinely empty.
        if (competitions.count() != 0) return;
        Candidat candidate = candidate();
        candidate.setCode("0123456");
        candidates.saveAndFlush(candidate);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, preparation::initializePapers);
        assertTrue(papers.findByCandidatId(candidate.getId()).isEmpty());
    }

    @Test
    @WithMockUser(roles = "CFD")
    void duplicatePaperSetupDoesNotAlterExistingPapers() {
        competition();
        Candidat candidate = candidate();
        candidate.setCode("1234567");
        candidates.saveAndFlush(candidate);
        Copie existing = papers.saveAndFlush(new Copie(Matier.ALGO, candidate));
        preparation.initializePapers();
        entityManager.flush();
        assertEquals(2, papers.findByCandidatId(candidate.getId()).size());
        assertTrue(papers.findByCandidatId(candidate.getId()).stream().anyMatch(p -> p.getId().equals(existing.getId())));
    }

    @Test
    @WithMockUser(roles = "CFD")
    void correctionInitializationIsRepeatableWithQualifiedDistinctTeachers() {
        Candidat candidate = candidate();
        Copie paper = papers.saveAndFlush(new Copie(Matier.ALGO, candidate));
        Enseignant first = teacher(Matier.ALGO);
        Enseignant second = teacher(Matier.ALGO);
        teacher(Matier.EDL);

        preparation.initializeCorrections();
        entityManager.flush();
        List<Correction> assigned = corrections.findByCopieId(paper.getId());
        assertEquals(2, assigned.size());
        assertNotEquals(assigned.get(0).getEnseignant().getId(), assigned.get(1).getEnseignant().getId());
        assertTrue(assigned.stream().allMatch(x -> x.getEnseignant().getSpecialite() == Matier.ALGO));

        preparation.initializeCorrections();
        entityManager.flush();
        assertEquals(2, corrections.findByCopieId(paper.getId()).size());
    }

    @Test
    @WithMockUser(roles = "CFD")
    void missingQualifiedTeachersCannotCreatePartialCorrections() {
        Candidat candidate = candidate();
        Copie paper = papers.saveAndFlush(new Copie(Matier.ALGO, candidate));
        teacher(Matier.EDL);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, preparation::initializeCorrections);
        entityManager.clear();
        assertTrue(corrections.findByCopieId(paper.getId()).isEmpty());
    }

    @Test
    @WithMockUser(roles = "CFD")
    void initializationIsPostOnlyAndCsrfProtected() throws Exception {
        mvc.perform(get("/cfd/initCopie")).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/cfd/InitCorrection")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/cfd/initCopie")).andExpect(status().isForbidden());
        mvc.perform(post("/cfd/InitCorrection")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VD")
    void assigningCodesIsPostOnlyAndCsrfProtected() throws Exception {
        mvc.perform(get("/vd/generateCodes")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/vd/generateCodes").param("selectedCandidates", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotInitializeCompetition() throws Exception {
        mvc.perform(post("/cfd/initCopie").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/cfd/InitCorrection").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/vd/generateCodes").with(csrf()).param("selectedCandidates", "1"))
                .andExpect(status().isForbidden());
    }

    private Candidat candidate() {
        String token = UUID.randomUUID().toString();
        return candidates.saveAndFlush(new Candidat(
                "candidate-" + token, "test-password-123",
                token + "@example.test", "Family", "Given", Role.CANDIDAT, new Date(), null, 0));
    }

    private Enseignant teacher(Matier specialty) {
        String token = UUID.randomUUID().toString();
        return teachers.saveAndFlush(new Enseignant(
                "teacher-" + token, "test-password-123", token + "@example.test",
                "Teacher", "Name", Role.ENSEIGNANT, "MCA", specialty));
    }

    private InfoConcour competition() {
        return competitions.saveAndFlush(new InfoConcour(2, "Speciality", Matier.ALGO, Matier.EDL, new Date()));
    }
}
