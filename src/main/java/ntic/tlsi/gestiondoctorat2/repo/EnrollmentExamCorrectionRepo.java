package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.EnrollmentExamCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentExamCorrectionRepo extends JpaRepository<EnrollmentExamCorrection, Long> {
    List<EnrollmentExamCorrection> findByPaper_Id(Long paperId);
    List<EnrollmentExamCorrection> findByPaper_IdOrderByIdAsc(Long paperId);
    List<EnrollmentExamCorrection> findByTeacher_UsernameOrderByIdAsc(String username);
}
