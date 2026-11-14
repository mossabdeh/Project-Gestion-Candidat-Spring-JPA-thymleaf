package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;
import ntic.tlsi.gestiondoctorat2.entities.*;

import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.text.DecimalFormat;
import java.util.*;


@Controller
@RequestMapping("/cfd")
public class CfdController extends BaseController{


    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private CFDRepo cfdRepo;
    @Autowired
    private CopieRepo copieRepo;
    @Autowired
    private InfoConRepo conRepo;
    @Autowired
    private EnseignantRepo enseignantRepo;
    @Autowired
    private CorrectionRepo correctionRepo;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.CompetitionPreparationService preparationService;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.ResultPublicationService resultService;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.EnrollmentExamPreparationService enrollmentExamPreparation;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.EnrollmentGradingService enrollmentGrading;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.EnrollmentResultPublicationService enrollmentResultPublication;



    @PostMapping("/assignEnrollmentThirdMarkers")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String assignEnrollmentThirdMarkers(@RequestParam("competitionId") Long competitionId) {
        enrollmentGrading.assignThirdMarkers(competitionId);
        return "redirect:/cfd/enrollmentPapers";
    }

    @GetMapping("/enrollmentResults")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String enrollmentResults(Model model) {
        conRepo.findTopByOrderByIdDesc().ifPresent(competition -> {
            model.addAttribute("competition", competition);
            if (competition.getResultsPublishedAt() == null) {
                // A failed validation is a conflict, never a partial score preview.
                // Preview only when the CFD explicitly requests it from this page.
                model.addAttribute("previewAvailable", true);
            }
        });
        return "CFDEnrollmentResults";
    }

    @GetMapping("/previewEnrollmentResults")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String previewEnrollmentResults(Model model) {
        var competition = conRepo.findTopByOrderByIdDesc()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "No competition configured"));
        if (competition.getResultsPublishedAt() != null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Results already published");
        }
        model.addAttribute("competition", competition);
        model.addAttribute("preview", enrollmentResultPublication.preview(competition.getId()));
        return "CFDEnrollmentResults";
    }

    @PostMapping("/publishEnrollmentResults")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String publishEnrollmentResults(@RequestParam("competitionId") Long competitionId) {
        enrollmentResultPublication.publish(competitionId);
        return "redirect:/cfd/enrollmentResults";
    }

    @GetMapping("/enrollmentPapers")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String enrollmentPapers(Model model) {
        conRepo.findTopByOrderByIdDesc().ifPresent(competition -> {
            model.addAttribute("competition", competition);
            model.addAttribute("papers", enrollmentExamPreparation.listPapers(competition.getId()));
        });
        return "CFDEnrollmentPapers";
    }

    @PostMapping("/initEnrollmentPapers")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String initEnrollmentPapers(@RequestParam("competitionId") Long competitionId) {
        enrollmentExamPreparation.initializePapers(competitionId);
        return "redirect:/cfd/enrollmentPapers";
    }

    @PostMapping("/initEnrollmentCorrections")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String initEnrollmentCorrections(@RequestParam("competitionId") Long competitionId) {
        enrollmentExamPreparation.initializeCorrections(competitionId);
        return "redirect:/cfd/enrollmentPapers";
    }

    @PostMapping("/initCopie")
    public String initializeCopies() {
        preparationService.initializePapers();
        return "redirect:/cfdPage";
    }

    @PostMapping("/InitCorrection")
    public String InitCorrection() {
        preparationService.initializeCorrections();
        return "redirect:/cfdPage";
    }

    @GetMapping("/getCorrectionCopie")
    public String getCorrectionCopie(Model model,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "5") int size,
                                     @RequestParam(name = "keyword", defaultValue = "") String keyword){


        Page<Copie> copiePage = copieRepo.findAll(PageRequest.of(page, size));
        model.addAttribute("copies", copiePage.getContent());
        model.addAttribute("pages", new int[copiePage.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "cfdCheckCorrection"; // Replace "correctionPage" with the actual name of your correction page view
    }

    @PostMapping("/ThirdCorrectionCheck")
    public String thirdCorrectionCheck() {
        List<Copie> copies = copieRepo.findAll();

        for (Copie copie : copies) {
            if (copie.getCorrections().size() < 2) continue;
            Correction correction1 = copie.getCorrections().get(0);
            Correction correction2 = copie.getCorrections().get(1);
            if (correction1.getNote() == null || correction2.getNote() == null) continue;
            double note1 = correction1.getNote();
            double note2 = correction2.getNote();

            if (Math.abs(note1 - note2) >= 3.0 || Math.abs(note2 - note1) >= 3.0) {
                List<Enseignant> enseignants = enseignantRepo.findBySpecialite(copie.getMatier());

                for (Enseignant enseignant : enseignants) {
                    if (enseignant != correction1.getEnseignant() && enseignant != correction2.getEnseignant()) {
                        Correction correction3 = new Correction();
                        correction3.setCopie(copie);
                        correction3.setEnseignant(enseignant);

                        copie.getCorrections().add(correction3);
                        copieRepo.save(copie);
                        correctionRepo.save(correction3);
                        break;  // Stop after assigning the third teacher
                    }
                }
            }
        }

        return "redirect:/cfd/getCorrectionCopie";
    }

    @GetMapping("/ResultatCandidats")
    public String ResultatCandidats(Model model,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "5") int size,
                                    @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        List<Candidat> candidatsWithNonNullCode = new ArrayList<>();
        candidatRepo.findAllBy().forEach(candidat -> {
            String code = candidat.getCode();
            if (code != null) {
                candidatsWithNonNullCode.add(candidat);
            }
        });

        Page<Candidat> pageCandidats = new PageImpl<>(candidatsWithNonNullCode, PageRequest.of(page, size), candidatsWithNonNullCode.size());
        model.addAttribute("ListCandidats", pageCandidats.getContent());
        model.addAttribute("pages", new int[pageCandidats.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "cfdResultatsCandidats";
    }






    @PostMapping("/CalculResultat")
    public String CalculResultat() {
        resultService.calculateResults();
        return "redirect:/cfd/ResultatCandidats";
    }

    @PostMapping("/SetPostForTopCandidates")
    public String setPostForTopCandidates() {
        resultService.rankCandidates();
        return "redirect:/cfd/ResultatCandidats";
    }

    @PostMapping("/PublishResults")
    public String publishResults() {
        resultService.publishResults();
        return "redirect:/cfd/ResultatCandidats";
    }

    @GetMapping("/getCFDs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getCFDs(Model model,
                         @RequestParam(name = "page",defaultValue = "0") int page,
                         @RequestParam(name = "size",defaultValue = "5") int size,
                         @RequestParam(name = "keyword",defaultValue = "") String keyword) {
        Page<CFD> pageCFD=cfdRepo.findByNomContains(keyword, PageRequest.of(page,size));
        model.addAttribute("ListCFDs",pageCFD.getContent());
        model.addAttribute("pages",new int[pageCFD.getTotalPages()]);
        model.addAttribute("currentPage",page);
        model.addAttribute("keyword",keyword);

        return "AdminCFD";
    }

    @GetMapping("/editCFD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editCFD(Model model, @RequestParam("id") Long id) {
        User cfd = cfdRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid cfd Id:" + id));
        model.addAttribute("cfd", cfd);
        return "AdminCFDEdit";
    }

    @PostMapping("/saveEditedCFD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedCFD(Model model, @Valid CFD cfd, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "AdminCFDEdit";
        }

        // Check if username already exists (excluding the current admin being edited)
        if (cfdRepo.existsByUsernameAndIdNot(cfd.getUsername(), cfd.getId())) {
            bindingResult.rejectValue("username", "error.cfd", "Username already exists");
            return "AdminCFDEdit";
        }

        // Retrieve the existing admin from the database
        User existingAdmin = cfdRepo.findById(cfd.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid cfd Id:" + cfd.getId()));

        // Set the existing password on the edited admin
        cfd.setPassword(existingAdmin.getPassword());

        cfd.setTypeRole(Role.CFD);
        cfd.setLogDate(new Date());

        cfdRepo.save(cfd);
        return "redirect:/cfd/getCFDs";
    }


}











