package ntic.tlsi.gestiondoctorat2;

import jakarta.persistence.EntityManager;
import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.ResultPublicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidateResultsPublicationTests {
    @Autowired MockMvc mvc;
    @Autowired ResultPublicationService publication;
    @Autowired CandidatRepo candidates;
    @Autowired InfoConRepo competitions;
    @Autowired CopieRepo papers;
    @Autowired CorrectionRepo corrections;
    @Autowired EnseignantRepo teachers;
    @Autowired EntityManager entityManager;

    @Test
    void publishingRanksCandidatesAndClearsStaleAdmission() {
        competition(1);
        Candidat high = candidate("top");
        Candidat low = candidate("bottom");
        gradedPapers(high, 16, 16, 14, 14);
        gradedPapers(low, 10, 10, 8, 8);
        low.setGetPoste(true);
        candidates.saveAndFlush(low);
        entityManager.clear();

        publication.publishResults();
        entityManager.flush();
        assertTrue(candidates.findCandidatOptionalById(high.getId()).orElseThrow().isGetPoste());
        assertFalse(candidates.findCandidatOptionalById(low.getId()).orElseThrow().isGetPoste());
        assertEquals(15.0, candidates.findCandidatOptionalById(high.getId()).orElseThrow().getMoyenneGeneral());
        assertEquals(9.0, candidates.findCandidatOptionalById(low.getId()).orElseThrow().getMoyenneGeneral());
        assertNotNull(competitions.findTopByOrderByIdDesc().orElseThrow().getResultsPublishedAt());
    }

    @Test
    void calculatingAndRankingDraftDoesNotPublish() {
        competition(1);
        Candidat candidate = candidate("draft");
        gradedPapers(candidate, 12, 12, 10, 10);
        entityManager.flush();
        entityManager.clear();

        publication.calculateResults();
        publication.rankCandidates();
        assertNull(competitions.findTopByOrderByIdDesc().orElseThrow().getResultsPublishedAt());
        assertTrue(candidates.findCandidatOptionalById(candidate.getId()).orElseThrow().isGetPoste());
    }

    @Test
    void incompleteGradesAreRejectedAndNotPublished() {
        competition(1);
        Candidat candidate = candidate("incomplete");
        Copie first = papers.saveAndFlush(new Copie(Matier.ALGO, candidate));
        papers.saveAndFlush(new Copie(Matier.EDL, candidate));
        teacherCorrection(first, Matier.ALGO, null);
        teacherCorrection(first, Matier.ALGO, 10.0);
        entityManager.clear();

        assertThrows(ResponseStatusException.class, publication::publishResults);
        assertNull(competitions.findTopByOrderByIdDesc().orElseThrow().getResultsPublishedAt());
    }

    @Test
    void thirdMarkerIsRequiredWhenFirstTwoMarksDifferByThreeOrMore() {
        competition(1);
        Candidat candidate = candidate("third");
        gradedPapers(candidate, 10, 14, 15, 15);
        entityManager.clear();
        assertThrows(ResponseStatusException.class, publication::publishResults);
        assertNull(competitions.findTopByOrderByIdDesc().orElseThrow().getResultsPublishedAt());
    }

    @Test
    void zeroMarksAreValidWhenAllRequiredCorrectionsArePresent() {
        competition(1);
        Candidat candidate = candidate("zero");
        gradedPapers(candidate, 0, 0, 0, 0);
        entityManager.clear();
        publication.publishResults();
        assertEquals(0.0, candidates.findCandidatOptionalById(candidate.getId()).orElseThrow().getMoyenneGeneral());
        assertTrue(candidates.findCandidatOptionalById(candidate.getId()).orElseThrow().isGetPoste());
    }

    @Test
    void publishingIsNotRepeatableAndFinalResultsCannotBeRecalculated() {
        competition(1);
        Candidat candidate = candidate("final");
        gradedPapers(candidate, 15, 15, 12, 12);
        entityManager.clear();
        publication.publishResults();
        assertThrows(ResponseStatusException.class, publication::publishResults);
        assertThrows(ResponseStatusException.class, publication::calculateResults);
        assertThrows(ResponseStatusException.class, publication::rankCandidates);
    }

    @Test
    @WithMockUser(username = "pending-result-candidate", roles = "CANDIDAT")
    void candidateSeesPendingStateInsteadOfRejectionBeforePublication() throws Exception {
        Candidat candidate = new Candidat("pending-result-candidate", "test-password-123",
                "pending@example.test", "Pending", "Candidate", Role.CANDIDAT,
                new Date(), "1234567", 0);
        candidate.setGetPoste(false);
        candidates.saveAndFlush(candidate);
        competition(1);

        String html = mvc.perform(get("/candidat/ResultatCandidat"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("resultsPublished", false))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Results have not been published yet"));
        assertFalse(html.contains("<b>Rejeté</b>"));
        assertFalse(html.contains("1234567"));
    }

    @Test
    @WithMockUser(roles = "CFD")
    void publicationRequiresPostAndCsrf() throws Exception {
        mvc.perform(get("/cfd/PublishResults"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/cfd/PublishResults"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotPublishResults() throws Exception {
        mvc.perform(post("/cfd/PublishResults").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher", roles = "ENSEIGNANT")
    void teacherCannotUpdateMarksAfterPublication() throws Exception {
        competition(1);
        Candidat candidate = candidate("marked");
        gradedPapers(candidate, 16, 16, 17, 17);
        entityManager.clear();
        publication.publishResults();
        mvc.perform(post("/enseignant/saveNotes").with(csrf())
                        .param("correctionIds", "1")
                        .param("notes", "11"))
                .andExpect(status().isConflict());
    }

    private InfoConcour competition(int places) {
        return competitions.saveAndFlush(new InfoConcour(places, "TLSI", Matier.ALGO, Matier.EDL, new Date()));
    }

    private Candidat candidate(String label) {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return candidates.saveAndFlush(new Candidat(
                "result-" + label + "-" + id, "test-password-123",
                id + "@example.test", "Family", "Given", Role.CANDIDAT,
                new Date(), id.substring(0, 7), 0));
    }

    private void gradedPapers(Candidat candidate, double a1, double a2, double b1, double b2) {
        Copie first = papers.saveAndFlush(new Copie(Matier.ALGO, candidate));
        Copie second = papers.saveAndFlush(new Copie(Matier.EDL, candidate));
        teacherCorrection(first, Matier.ALGO, a1);
        teacherCorrection(first, Matier.ALGO, a2);
        teacherCorrection(second, Matier.EDL, b1);
        teacherCorrection(second, Matier.EDL, b2);
    }

    private void teacherCorrection(Copie paper, Matier specialty, Double note) {
        String id = UUID.randomUUID().toString();
        Enseignant teacher = teachers.saveAndFlush(new Enseignant(
                "teacher-" + id, "test-password-123",
                id + "@example.test", "Teacher", "Test", Role.ENSEIGNANT, "MCA", specialty));
        Correction correction = new Correction();
        correction.setCopie(paper);
        correction.setEnseignant(teacher);
        correction.setNote(note);
        corrections.saveAndFlush(correction);
    }
}
