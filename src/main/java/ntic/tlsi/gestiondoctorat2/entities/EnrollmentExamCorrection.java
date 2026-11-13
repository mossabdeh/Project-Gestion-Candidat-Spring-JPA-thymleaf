package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;

/** One assigned teacher per enrollment paper; null grade means not marked. */
@Entity
@Table(name = "enrollment_exam_correction", uniqueConstraints =
        @UniqueConstraint(name = "uk_enrollment_paper_teacher",
                columnNames = {"paper_id", "teacher_id"}))
public class EnrollmentExamCorrection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paper_id", nullable = false, updatable = false)
    private EnrollmentExamPaper paper;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false, updatable = false)
    private Enseignant teacher;

    private Double grade;

    protected EnrollmentExamCorrection() {}

    public EnrollmentExamCorrection(EnrollmentExamPaper paper, Enseignant teacher) {
        if (paper == null || teacher == null || teacher.getSpecialite() != paper.getSubject()) {
            throw new IllegalArgumentException("A qualified teacher and paper are required");
        }
        this.paper = paper;
        this.teacher = teacher;
    }

    public Long getId() { return id; }
    public EnrollmentExamPaper getPaper() { return paper; }
    public Enseignant getTeacher() { return teacher; }
    public Double getGrade() { return grade; }

    public void recordGrade(double value) {
        if (!Double.isFinite(value) || value < 0 || value > 20) {
            throw new IllegalArgumentException("Grade must be a finite number between 0 and 20");
        }
        this.grade = value;
    }
}
