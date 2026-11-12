package ntic.tlsi.gestiondoctorat2.entities;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * One Candidate's registration for one competition.
 * Exam scores/copies still live on Candidat until the later migration.
 */
@Entity
@Table(name = "competition_enrollment",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_enrollment_competition_candidate",
           columnNames = {"competition_id", "candidate_id"}))
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
}
