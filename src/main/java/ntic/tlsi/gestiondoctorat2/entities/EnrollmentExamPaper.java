package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;

/** One subject paper for one enrollment; independent of legacy Copie. */
@Entity
@Table(name = "enrollment_exam_paper", uniqueConstraints =
        @UniqueConstraint(name = "uk_enrollment_paper_subject",
                columnNames = {"enrollment_id", "subject"}))
public class EnrollmentExamPaper {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private CompetitionEnrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject", nullable = false, updatable = false)
    private Matier subject;

    protected EnrollmentExamPaper() {}

    public EnrollmentExamPaper(CompetitionEnrollment enrollment, Matier subject) {
        if (enrollment == null || subject == null) {
            throw new IllegalArgumentException("Enrollment and subject are required");
        }
        this.enrollment = enrollment;
        this.subject = subject;
    }

    public Long getId() { return id; }
    public CompetitionEnrollment getEnrollment() { return enrollment; }
    public Matier getSubject() { return subject; }
}
