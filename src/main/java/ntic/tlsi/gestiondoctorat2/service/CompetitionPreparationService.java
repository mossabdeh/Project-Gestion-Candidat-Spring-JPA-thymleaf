package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;

/**
 * Initializes competition resources without changing them when a button is
 * submitted more than once. The current schema supports one exam per candidate;
 * multiple competition enrollments require a separate model migration.
 */
@Service
public class CompetitionPreparationService {
    private final CandidatRepo candidates;
    private final InfoConRepo competitions;
    private final CopieRepo papers;
    private final CorrectionRepo corrections;
    private final EnseignantRepo teachers;
    private final SecureRandom secureRandom = new SecureRandom();

    public CompetitionPreparationService(CandidatRepo candidates, InfoConRepo competitions,
                                         CopieRepo papers, CorrectionRepo corrections,
                                         EnseignantRepo teachers) {
        this.candidates = candidates;
        this.competitions = competitions;
        this.papers = papers;
        this.corrections = corrections;
        this.teachers = teachers;
    }

    @Transactional
    public void assignCodes(List<Long> selectedCandidateIds) {
        if (selectedCandidateIds == null || selectedCandidateIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one candidate");
        }

        // Resolve and validate the entire request before making any changes.
        // A bad ID must not cause a partial assignment even when callers join
        // an existing transaction.
        List<Candidat> requested = new ArrayList<>();
        for (Long candidateId : new LinkedHashSet<>(selectedCandidateIds)) {
            Candidat candidate = candidates.findCandidatOptionalById(candidateId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
            requested.add(candidate);
        }

        Set<String> used = new HashSet<>(candidates.findAssignedCodes());
        for (Candidat candidate : requested) {
            // Generating a code again must never invalidate an existing exam paper.
            if (candidate.getCode() != null && !candidate.getCode().isBlank()) {
                used.add(candidate.getCode());
                continue;
            }
            String generated = null;
            for (int attempt = 0; attempt < 10000; attempt++) {
                String code = generateCode();
                if (used.add(code)) {
                    generated = code;
                    break;
                }
            }
            if (generated == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not allocate a unique exam code");
            }
            candidate.setCode(generated);
        }
    }

    @Transactional
    public void initializePapers() {
        InfoConcour competition = competitions.findTopByOrderByIdDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Create a competition first"));
        Matier first = competition.getMatier1();
        Matier second = competition.getMatier2();
        if (first == null || second == null || first == second) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A competition needs two distinct subjects");
        }
        for (Candidat candidate : candidates.findAllBy()) {
            if (candidate.getCode() == null || candidate.getCode().isBlank()) continue;
            List<Copie> existing = papers.findByCandidatId(candidate.getId());
            Set<Matier> present = new HashSet<>();
            for (Copie paper : existing) {
                if (paper.getMatier() != first && paper.getMatier() != second) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Candidate has exam papers from a different competition");
                }
                if (!present.add(paper.getMatier())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Candidate already has duplicate exam papers");
                }
            }
            if (!present.contains(first)) papers.save(new Copie(first, candidate));
            if (!present.contains(second)) papers.save(new Copie(second, candidate));
        }
    }

    @Transactional
    public void initializeCorrections() {
        List<Enseignant> availableTeachers = teachers.findAllBy();
        for (Copie paper : papers.findAll()) {
            List<Correction> assigned = corrections.findByCopieId(paper.getId());
            Set<Long> assignedTeacherIds = new HashSet<>();
            for (Correction correction : assigned) {
                if (correction.getEnseignant() == null || !assignedTeacherIds.add(correction.getEnseignant().getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid existing correction assignment");
                }
            }
            for (Enseignant teacher : availableTeachers) {
                if (assigned.size() >= 2) break;
                if (teacher.getSpecialite() != paper.getMatier()) continue;
                if (!assignedTeacherIds.add(teacher.getId())) continue;
                Correction correction = new Correction();
                correction.setCopie(paper);
                correction.setEnseignant(teacher);
                corrections.save(correction);
                assigned.add(correction);
            }
            if (assigned.size() < 2) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Each exam paper needs two distinct qualified teachers");
            }
        }
    }

    private String generateCode() {
        List<Character> digits = new ArrayList<>();
        for (char digit = '0'; digit <= '9'; digit++) digits.add(digit);
        Collections.shuffle(digits, secureRandom);
        StringBuilder result = new StringBuilder(7);
        for (int i = 0; i < 7; i++) result.append(digits.get(i));
        return result.toString();
    }
}
