package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.EnrollmentAnonymizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dedicated test database: workers run in real, independently committed transactions.
 * Never leak concurrency fixtures into tests using the default in-memory database.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:anonymization-concurrency;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AnonymizationConcurrencyPrivacyTests {
    @Autowired EnrollmentAnonymizationService anonymization;
    @Autowired InfoConRepo competitions;
    @Autowired CompetitionEnrollmentRepo enrollments;
    @Autowired CandidatRepo candidates;
    @Autowired EnseignantRepo teachers;
    @Autowired EnrollmentExamPaperRepo papers;
    @Autowired EnrollmentExamCorrectionRepo corrections;
    @Autowired MockMvc mvc;

    @Test
    void simultaneousCodeAllocationsAreUniqueAndIdempotent() throws Exception {
        InfoConcour competition = competition();
        for (int i = 0; i < 4; i++) {
            enrollments.saveAndFlush(new CompetitionEnrollment(competition, candidate()));
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Integer> allocate = () -> {
                start.await();
                return anonymization.assignCodes(competition.getId());
            };
            Future<Integer> first = pool.submit(allocate);
            Future<Integer> second = pool.submit(allocate);
            start.countDown();
            assertEquals(4, first.get(20, TimeUnit.SECONDS) + second.get(20, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        var allocated = enrollments.findByCompetition_IdOrderByIdAsc(competition.getId());
        List<String> codes = allocated.stream().map(CompetitionEnrollment::getAnonymousCode).toList();
        assertEquals(4, codes.size());
        assertTrue(codes.stream().allMatch(code -> code != null && code.matches("[0-9]{7}")));
        assertEquals(4, Set.copyOf(codes).size());
        assertEquals(0, anonymization.assignCodes(competition.getId()));
        assertEquals(codes, enrollments.findByCompetition_IdOrderByIdAsc(competition.getId())
                .stream().map(CompetitionEnrollment::getAnonymousCode).toList());
    }

    @Test
    @WithMockUser(username = "privacy-teacher", roles = "ENSEIGNANT")
    void teacherGradingShowsOnlyAnonymousCodeNotCandidateIdentity() throws Exception {
        InfoConcour competition = competition();
        Candidat candidate = candidate();
        CompetitionEnrollment enrollment = new CompetitionEnrollment(competition, candidate);
        enrollment.assignAnonymousCode("0098123");
        enrollment = enrollments.saveAndFlush(enrollment);
        EnrollmentExamPaper paper = papers.saveAndFlush(new EnrollmentExamPaper(enrollment, Matier.ALGO));
        Enseignant teacher = teachers.saveAndFlush(new Enseignant("privacy-teacher",
                "test-password-12345", UUID.randomUUID() + "@example.test",
                "Teacher", "One", Role.ENSEIGNANT, "MCA", Matier.ALGO));
        corrections.saveAndFlush(new EnrollmentExamCorrection(paper, teacher));

        String html = mvc.perform(get("/enseignant/enrollmentGrades"))
                .andExpect(status().isOk())
                .andExpect(view().name("EnseignantEnrollmentGrades"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("0098123"));
        assertFalse(html.contains(candidate.getUsername()));
        assertFalse(html.contains(candidate.getNom()));
        assertFalse(html.contains(candidate.getEmail()));
    }

    private InfoConcour competition() {
        return competitions.saveAndFlush(new InfoConcour(2, "Privacy", Matier.ALGO, Matier.EDL, new Date()));
    }

    private Candidat candidate() {
        String id = UUID.randomUUID().toString();
        return candidates.saveAndFlush(new Candidat("anonymous-candidate-" + id,
                "test-password-12345", id + "@example.test",
                "PrivateFamily-" + id, "PrivateGiven", Role.CANDIDAT, new Date(), null, 0));
    }
}
