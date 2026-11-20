package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.EnrollmentExamPreparationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Real separate transactions exercise the competition-row lock, not a shared test transaction. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EnrollmentPreparationConcurrencyTests {
    @Autowired EnrollmentExamPreparationService preparation;
    @Autowired InfoConRepo competitions;
    @Autowired CandidatRepo candidates;
    @Autowired CompetitionEnrollmentRepo enrollments;
    @Autowired EnrollmentExamPaperRepo papers;
    @Autowired EnrollmentExamCorrectionRepo corrections;
    @Autowired EnseignantRepo teachers;

    @Test
    void parallelPreparationRequestsNeverCreateDuplicatePapersOrAssignments() throws Exception {
        InfoConcour competition = competitions.saveAndFlush(
                new InfoConcour(2, "Concurrent CFD", Matier.ALGO, Matier.EDL, new Date()));
        String id = UUID.randomUUID().toString();
        Candidat candidate = candidates.saveAndFlush(new Candidat(
                "concurrent-candidate-" + id, "test-password-12345", id + "@example.test",
                "Concurrent", "Candidate", Role.CANDIDAT, new Date(), null, 0));
        CompetitionEnrollment enrolled = new CompetitionEnrollment(competition, candidate);
        enrolled.assignAnonymousCode("0000991");
        enrollments.saveAndFlush(enrolled);

        assertEquals(2, runConcurrently(() -> preparation.initializePapers(competition.getId())));
        assertEquals(2, papers.findByEnrollment_Competition_IdOrderByIdAsc(competition.getId()).size());

        for (Matier subject : new Matier[]{Matier.ALGO, Matier.EDL}) {
            for (int i = 0; i < 2; i++) {
                String token = UUID.randomUUID().toString();
                teachers.saveAndFlush(new Enseignant("concurrent-teacher-" + token,
                        "test-password-12345", token + "@example.test",
                        "Qualified", "Teacher", Role.ENSEIGNANT, "MCA", subject));
            }
        }
        assertEquals(4, runConcurrently(() -> preparation.initializeCorrections(competition.getId())));
        for (EnrollmentExamPaper paper : papers.findByEnrollment_Competition_IdOrderByIdAsc(competition.getId())) {
            var assigned = corrections.findByPaper_Id(paper.getId());
            assertEquals(2, assigned.size());
            assertNotEquals(assigned.get(0).getTeacher().getId(), assigned.get(1).getTeacher().getId());
        }
    }

    private int runConcurrently(Callable<Integer> action) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch begin = new CountDownLatch(1);
        try {
            Callable<Integer> task = () -> {
                begin.await();
                return action.call();
            };
            Future<Integer> first = pool.submit(task);
            Future<Integer> second = pool.submit(task);
            begin.countDown();
            return first.get(20, TimeUnit.SECONDS) + second.get(20, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }
}
