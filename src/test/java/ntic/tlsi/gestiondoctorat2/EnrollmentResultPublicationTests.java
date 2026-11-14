package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.CandidateResultsReadService;
import ntic.tlsi.gestiondoctorat2.service.EnrollmentResultPublicationService;
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
class EnrollmentResultPublicationTests {
    @Autowired EnrollmentResultPublicationService publication;
    @Autowired CandidateResultsReadService candidateView;
    @Autowired ResultPublicationService legacyPublication;
    @Autowired EnrollmentResultSnapshotRepo snapshots;
    @Autowired CompetitionEnrollmentRepo enrollments;
    @Autowired EnrollmentExamPaperRepo papers;
    @Autowired EnrollmentExamCorrectionRepo corrections;
    @Autowired InfoConRepo competitions;
    @Autowired CandidatRepo candidates;
    @Autowired EnseignantRepo teachers;
    @Autowired MockMvc mvc;

    @Test
    void publishesOrderedAveragesRanksAndImmutableCandidateView() {
        InfoConcour competition = competition(1);
        Candidat top = candidate();
        Candidat bottom = candidate();
        registration(competition, top, "0000001", 16, 16, 14, 14);
        registration(competition, bottom, "0000002", 10, 10, 8, 8);

        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.PENDING,
                candidateView.readFor(top.getUsername()).status());
        assertEquals(2, publication.preview(competition.getId()).size());
        assertEquals(0, snapshots.count());
        assertEquals(2, publication.publish(competition.getId()));
        assertNotNull(competition.getResultsPublishedAt());
        assertEquals(2, snapshots.count());
        var first = candidateView.readFor(top.getUsername());
        var second = candidateView.readFor(bottom.getUsername());
        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.PUBLISHED, first.status());
        assertEquals("0000001", first.code());
        assertEquals(15.0, first.overallAverage().doubleValue());
        assertEquals(Matier.ALGO, first.subjects().get(0).subject());
        assertEquals(16.0, first.subjects().get(0).average());
        assertEquals(Matier.EDL, first.subjects().get(1).subject());
        assertEquals(14.0, first.subjects().get(1).average());
        assertEquals(Boolean.TRUE, first.admitted());
        assertEquals(9.0, second.overallAverage().doubleValue());
        assertEquals(Boolean.FALSE, second.admitted());
        assertEquals(1, snapshots.findByEnrollment_Id(
                enrollments.findByCompetition_IdAndCandidate_Username(
                    competition.getId(), top.getUsername()).orElseThrow().getId()).orElseThrow().getRank());
        assertThrows(ResponseStatusException.class, () -> publication.publish(competition.getId()));
        assertEquals(2, snapshots.count());
        assertNull(top.getCode()); // Never update legacy Candidate-wide scores or codes.
    }

    @Test
    void deterministicTieBreakUsesEnrollmentId() {
        InfoConcour competition = competition(1);
        Candidat first = candidate();
        Candidat second = candidate();
        var e1 = registration(competition, first, "0000003", 12, 12, 12, 12);
        var e2 = registration(competition, second, "0000004", 12, 12, 12, 12);
        assertEquals(2, publication.publish(competition.getId()));
        assertTrue(snapshots.findByEnrollment_Id(e1.getId()).orElseThrow().isAdmitted());
        assertFalse(snapshots.findByEnrollment_Id(e2.getId()).orElseThrow().isAdmitted());
        assertEquals(1, snapshots.findByEnrollment_Id(e1.getId()).orElseThrow().getRank());
        assertEquals(2, snapshots.findByEnrollment_Id(e2.getId()).orElseThrow().getRank());
    }

    @Test
    void rejectsIncompleteMarksWithoutAnyPublishedSnapshot() {
        InfoConcour competition = competition(1);
        Candidat user = candidate();
        registration(competition, user, "0000005", 10, 10, 10, 10);
        EnrollmentExamPaper first = papers.findByEnrollment_Competition_IdOrderByIdAsc(competition.getId()).get(0);
        corrections.findByPaper_IdOrderByIdAsc(first.getId()).get(0).recordGrade(5);
        // Discrepancy of >=3 requires a third qualified marker.
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> publication.publish(competition.getId())).getStatusCode());
        assertNull(competition.getResultsPublishedAt());
        assertEquals(0, snapshots.count());
    }

    @Test
    void publishedEnrollmentDoesNotLeakHistoricalOrOtherCandidateGrades() {
        Candidat participant = candidate();
        InfoConcour previous = competition(1);
        registration(previous, participant, "0000006", 18, 18, 18, 18);
        publication.publish(previous.getId());

        InfoConcour next = competition(1);
        Candidat other = candidate();
        registration(next, other, "0000007", 4, 4, 4, 4);
        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.PENDING,
                candidateView.readFor(participant.getUsername()).status());
        publication.publish(next.getId());
        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.NOT_AVAILABLE,
                candidateView.readFor(participant.getUsername()).status());
        assertEquals(4.0, candidateView.readFor(other.getUsername()).overallAverage().doubleValue());
    }

    @Test
    void legacyPublicationIsRejectedIfCurrentCompetitionHasEnrollments() {
        InfoConcour competition = competition(1);
        registration(competition, candidate(), "0000008", 12, 12, 12, 12);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, legacyPublication::publishResults).getStatusCode());
        assertNull(competition.getResultsPublishedAt());
    }

    @Test
    @WithMockUser(roles = "CFD")
    void scopedPublicationUsesPostAndCsrf() throws Exception {
        mvc.perform(get("/cfd/enrollmentResults"))
                .andExpect(status().isOk())
                .andExpect(view().name("CFDEnrollmentResults"));
        mvc.perform(get("/cfd/publishEnrollmentResults"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/cfd/publishEnrollmentResults").param("competitionId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotPreviewOrPublishResults() throws Exception {
        mvc.perform(get("/cfd/enrollmentResults")).andExpect(status().isForbidden());
        mvc.perform(get("/cfd/previewEnrollmentResults")).andExpect(status().isForbidden());
        mvc.perform(post("/cfd/publishEnrollmentResults").with(csrf())
                .param("competitionId", "1")).andExpect(status().isForbidden());
    }

    private InfoConcour competition(int places) {
        return competitions.saveAndFlush(new InfoConcour(places, "Snapshot test", Matier.ALGO,
                Matier.EDL, new Date()));
    }

    private Candidat candidate() {
        String token = UUID.randomUUID().toString();
        return candidates.saveAndFlush(new Candidat("snapshot-" + token,
                "test-password-12345", token + "@example.test",
                "Demo", "Candidate", Role.CANDIDAT, new Date(), null, 0));
    }

    private CompetitionEnrollment registration(InfoConcour comp, Candidat candidate, String code,
                                                double a1, double a2, double b1, double b2) {
        CompetitionEnrollment enrollment = new CompetitionEnrollment(comp, candidate);
        enrollment.assignAnonymousCode(code);
        enrollments.saveAndFlush(enrollment);
        paper(enrollment, Matier.ALGO, a1, a2);
        paper(enrollment, Matier.EDL, b1, b2);
        return enrollment;
    }

    private void paper(CompetitionEnrollment enrollment, Matier subject, double first, double second) {
        EnrollmentExamPaper paper = papers.saveAndFlush(new EnrollmentExamPaper(enrollment, subject));
        mark(paper, subject, first);
        mark(paper, subject, second);
    }

    private void mark(EnrollmentExamPaper paper, Matier subject, double value) {
        String token = UUID.randomUUID().toString();
        Enseignant teacher = teachers.saveAndFlush(new Enseignant("snapshot-teacher-" + token,
                "test-password-12345", token + "@example.test",
                "Demo", "Teacher", Role.ENSEIGNANT, "MCA", subject));
        EnrollmentExamCorrection correction = new EnrollmentExamCorrection(paper, teacher);
        correction.recordGrade(value);
        corrections.saveAndFlush(correction);
    }
}
