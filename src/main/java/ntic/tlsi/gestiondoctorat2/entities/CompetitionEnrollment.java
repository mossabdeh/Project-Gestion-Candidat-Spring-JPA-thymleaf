package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * One Candidate's registration for one competition.
 * Exam scores/copies still live on Candidat until the later migration.
 */
@Entity
@Table(name = "competition_enrollment",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_enrollment_competition_candidate",
                   columnNames = {"competition_id", "candidate_id"}),
           @UniqueConstraint(name = "uk_enrollment_competition_code",
                   columnNames = {"competition_id", "anonymous_code"})
       })
public class CompetitionEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private InfoConcour competition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidat candidate;

    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    // Unique only within one competition. Null means not yet anonymized.
    @Column(name = "anonymous_code", length = 7)
    private String anonymousCode;

    protected CompetitionEnrollment() {}

    public CompetitionEnrollment(InfoConcour competition, Candidat candidate) {
        this.competition = competition;
        this.candidate = candidate;
        this.registeredAt = Instant.now();
    }

    public Long getId() { return id; }
    public InfoConcour getCompetition() { return competition; }
    public Candidat getCandidate() { return candidate; }
    public Instant getRegisteredAt() { return registeredAt; }
    public String getAnonymousCode() { return anonymousCode; }
    public void assignAnonymousCode(String code) {
        if (code == null || !code.matches("[0-9]{7}")) {
            throw new IllegalArgumentException("Anonymization code must contain seven digits");
        }
        if (anonymousCode != null && !anonymousCode.equals(code)) {
            throw new IllegalStateException("An already assigned anonymization code cannot change");
        }
        this.anonymousCode = code;
    }
}
