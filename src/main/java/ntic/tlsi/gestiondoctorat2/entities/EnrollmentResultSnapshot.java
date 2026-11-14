package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A frozen publication record. One row per enrollment; no mutable entity
 * setters and no cascade to change any marks after publication.
 */
@Entity
@Table(name = "enrollment_result_snapshot",
       uniqueConstraints = @UniqueConstraint(name = "uk_result_enrollment", columnNames = "enrollment_id"))
public class EnrollmentResultSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true, updatable = false)
    private CompetitionEnrollment enrollment;

    @Column(nullable = false, updatable = false)
    private String anonymousCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Matier firstSubject;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Matier secondSubject;
    @Column(nullable = false, updatable = false)
    private double firstAverage;
    @Column(nullable = false, updatable = false)
    private double secondAverage;
    @Column(nullable = false, updatable = false)
    private double overallAverage;
    @Column(nullable = false, updatable = false)
    private int rank;
    @Column(nullable = false, updatable = false)
    private boolean admitted;
    @Column(nullable = false, updatable = false)
    private Instant publishedAt;

    protected EnrollmentResultSnapshot() {}

    public EnrollmentResultSnapshot(CompetitionEnrollment enrollment, Matier firstSubject,
                                    Matier secondSubject, double firstAverage,
                                    double secondAverage, double overallAverage,
                                    int rank, boolean admitted, Instant publishedAt) {
        if (enrollment == null || enrollment.getAnonymousCode() == null ||
                firstSubject == null || secondSubject == null || firstSubject == secondSubject ||
                rank < 1 || publishedAt == null) {
            throw new IllegalArgumentException("Complete publication data required");
        }
        this.enrollment = enrollment;
        this.anonymousCode = enrollment.getAnonymousCode();
        this.firstSubject = firstSubject;
        this.secondSubject = secondSubject;
        this.firstAverage = firstAverage;
        this.secondAverage = secondAverage;
        this.overallAverage = overallAverage;
        this.rank = rank;
        this.admitted = admitted;
        this.publishedAt = publishedAt;
    }

    public Long getId() { return id; }
    public CompetitionEnrollment getEnrollment() { return enrollment; }
    public String getAnonymousCode() { return anonymousCode; }
    public Matier getFirstSubject() { return firstSubject; }
    public Matier getSecondSubject() { return secondSubject; }
    public double getFirstAverage() { return firstAverage; }
    public double getSecondAverage() { return secondAverage; }
    public double getOverallAverage() { return overallAverage; }
    public int getRank() { return rank; }
    public boolean isAdmitted() { return admitted; }
    public Instant getPublishedAt() { return publishedAt; }
}
