package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.EnrollmentExamPaper;
import ntic.tlsi.gestiondoctorat2.entities.Enseignant;

/**
 * Minimum identity-based conflict guard. A more complete institutional
 * conflict-of-interest registry would require explicit relationship data.
 */
final class ExaminerEligibility {
    private ExaminerEligibility() {}

    static boolean canMark(Enseignant teacher, EnrollmentExamPaper paper) {
        if (teacher == null || paper == null || teacher.getSpecialite() != paper.getSubject()) {
            return false;
        }
        Candidat candidate = paper.getEnrollment().getCandidate();
        return !sameNonBlank(teacher.getUsername(), candidate.getUsername())
                && !sameNonBlank(teacher.getEmail(), candidate.getEmail());
    }

    private static boolean sameNonBlank(String left, String right) {
        return left != null && right != null && !left.isBlank() && !right.isBlank()
                && left.trim().equalsIgnoreCase(right.trim());
    }
}
