package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.repo.CompetitionEnrollmentRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

/**
 * In the current single-exam model, scores and admission decisions are drafts
 * until the latest competition is explicitly published by the CFD role.
 * Policy: average of two (or three) marks per subject, equal subject weight,
 * and ties resolved by ascending candidate ID after descending average.
 */
@Service
public class ResultPublicationService {
    private final CandidatRepo candidates;
    private final InfoConRepo competitions;
    private final CompetitionEnrollmentRepo enrollments;

    public ResultPublicationService(CandidatRepo candidates, InfoConRepo competitions,
                                    CompetitionEnrollmentRepo enrollments) {
        this.candidates = candidates;
        this.competitions = competitions;
        this.enrollments = enrollments;
    }

    @Transactional
    public void calculateResults() {
        InfoConcour competition = unpublishedCompetition();
        List<Score> scores = scoreAll(competition);
        writeScores(scores);
        for (Candidat candidate : candidates.findAllBy()) candidate.setGetPoste(false);
    }

    @Transactional
    public void rankCandidates() {
        InfoConcour competition = unpublishedCompetition();
        List<Score> scores = scoreAll(competition);
        writeScores(scores);
        assignPlaces(competition, scores);
    }

    @Transactional
    public void publishResults() {
        InfoConcour competition = unpublishedCompetition();
        List<Score> scores = scoreAll(competition);
        writeScores(scores);
        assignPlaces(competition, scores);
        competition.setResultsPublishedAt(Instant.now());
    }

    private InfoConcour unpublishedCompetition() {
        InfoConcour competition = competitions.findTopByOrderByIdDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Competition not configured"));
        if (enrollments.existsByCompetition_Id(competition.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Use enrollment-scoped publication for registered candidates");
        }
        if (competition.getResultsPublishedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Results are already published");
        }
        if (competition.getMatier1() == null || competition.getMatier2() == null
                || competition.getMatier1() == competition.getMatier2()
                || competition.getNbrPostes() == null || competition.getNbrPostes() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid competition configuration");
        }
        return competition;
    }

    private List<Score> scoreAll(InfoConcour competition) {
        List<Score> scores = new ArrayList<>();
        for (Candidat candidate : candidates.findAllBy()) {
            if (candidate.getCode() == null || candidate.getCode().isBlank()) continue;
            Map<Matier, Copie> bySubject = new EnumMap<>(Matier.class);
            for (Copie paper : candidate.getCopies()) {
                if (paper.getMatier() == null || bySubject.putIfAbsent(paper.getMatier(), paper) != null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate or invalid exam paper");
                }
            }
            if (bySubject.size() != 2 || !bySubject.containsKey(competition.getMatier1())
                    || !bySubject.containsKey(competition.getMatier2())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Candidate has incomplete exam papers");
            }
            double first = average(bySubject.get(competition.getMatier1()));
            double second = average(bySubject.get(competition.getMatier2()));
            scores.add(new Score(candidate, round(first), round(second), round((first + second) / 2)));
        }
        if (scores.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "No candidates are ready");
        return scores;
    }

    private double average(Copie paper) {
        List<Correction> corrections = paper.getCorrections();
        if (corrections.size() < 2 || corrections.size() > 3) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Paper needs two or three corrections");
        }
        Set<Long> markers = new HashSet<>();
        double total = 0;
        List<Double> marks = new ArrayList<>();
        for (Correction correction : corrections) {
            Double value = correction.getNote();
            if (correction.getEnseignant() == null
                    || !markers.add(correction.getEnseignant().getId())
                    || value == null || !Double.isFinite(value) || value < 0 || value > 20) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Incomplete or invalid grades");
            }
            total += value;
            marks.add(value);
        }
        if (Math.abs(marks.get(0) - marks.get(1)) >= 3 && corrections.size() < 3) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A third grade is required");
        }
        return total / corrections.size();
    }

    private void writeScores(List<Score> scores) {
        for (Score score : scores) {
            score.candidate.setMoyMatier1(score.first);
            score.candidate.setMoyMatier2(score.second);
            score.candidate.setMoyenneGeneral(score.overall);
        }
    }

    private void assignPlaces(InfoConcour competition, List<Score> scores) {
        // Clear stale admissions before applying the current order.
        for (Candidat candidate : candidates.findAllBy()) candidate.setGetPoste(false);
        scores.sort(Comparator.comparingDouble((Score s) -> s.overall).reversed()
                .thenComparing(s -> s.candidate.getId()));
        for (int i = 0; i < Math.min(competition.getNbrPostes(), scores.size()); i++) {
            scores.get(i).candidate.setGetPoste(true);
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Score(Candidat candidate, double first, double second, double overall) {}
}
