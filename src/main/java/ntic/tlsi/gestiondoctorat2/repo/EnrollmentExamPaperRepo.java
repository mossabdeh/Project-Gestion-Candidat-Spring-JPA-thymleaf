package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.EnrollmentExamPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentExamPaperRepo extends JpaRepository<EnrollmentExamPaper, Long> {
    List<EnrollmentExamPaper> findByEnrollment_Competition_IdOrderByIdAsc(Long competitionId);
    List<EnrollmentExamPaper> findByEnrollment_Id(Long enrollmentId);
}
