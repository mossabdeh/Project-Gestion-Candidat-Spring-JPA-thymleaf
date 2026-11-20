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
    private StaffProfileService staffProfiles;
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



    private Optional<InfoConcour> chosenCompetition(Long competitionId) {
        if (competitionId == null) return conRepo.findTopByOrderByIdDesc();
        return Optional.of(conRepo.findById(competitionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Competition not found")));
    }

    @PostMapping("/assignEnrollmentThirdMarkers")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String assignEnrollmentThirdMarkers(@RequestParam("competitionId") Long competitionId) {
        enrollmentGrading.assignThirdMarkers(competitionId);
        return "redirect:/cfd/enrollmentPapers?competitionId=" + competitionId;
    }

    @GetMapping("/enrollmentResults")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String enrollmentResults(Model model,
                                    @RequestParam(name = "competitionId", required = false) Long competitionId) {
        model.addAttribute("competitions", conRepo.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "id")));
        chosenCompetition(competitionId).ifPresent(competition -> {
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
    public String previewEnrollmentResults(Model model,
                                          @RequestParam(name = "competitionId", required = false) Long competitionId) {
        model.addAttribute("competitions", conRepo.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "id")));
        var competition = chosenCompetition(competitionId)
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
        return "redirect:/cfd/enrollmentResults?competitionId=" + competitionId;
    }

    @GetMapping("/enrollmentPapers")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String enrollmentPapers(Model model,
                                   @RequestParam(name = "competitionId", required = false) Long competitionId) {
        model.addAttribute("competitions", conRepo.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "id")));
        chosenCompetition(competitionId).ifPresent(competition -> {
            model.addAttribute("competition", competition);
            model.addAttribute("papers", enrollmentExamPreparation.listPapers(competition.getId()));
        });
        return "CFDEnrollmentPapers";
    }

    @PostMapping("/initEnrollmentPapers")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String initEnrollmentPapers(@RequestParam("competitionId") Long competitionId) {
        enrollmentExamPreparation.initializePapers(competitionId);
        return "redirect:/cfd/enrollmentPapers?competitionId=" + competitionId;
    }

    @PostMapping("/initEnrollmentCorrections")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String initEnrollmentCorrections(@RequestParam("competitionId") Long competitionId) {
        enrollmentExamPreparation.initializeCorrections(competitionId);
        return "redirect:/cfd/enrollmentPapers?competitionId=" + competitionId;
    }

    /**
     * Old Candidate-wide paper initialization is retired. New submissions must
     * use the selected competition's enrollment-scoped preparation endpoints.
     */
    @PostMapping("/initCopie")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String initializeCopies() {
        throw retiredPreparation();
    }

    @PostMapping("/InitCorrection")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String InitCorrection() {
        throw retiredPreparation();
    }

    @GetMapping("/getCorrectionCopie")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String getCorrectionCopie() {
        return "redirect:/cfd/enrollmentPapers";
    }

    @PostMapping("/ThirdCorrectionCheck")
    @PreAuthorize("hasRole('ROLE_CFD')")
    public String thirdCorrectionCheck() {
        throw retiredPreparation();
    }

    private org.springframework.web.server.ResponseStatusException retiredPreparation() {
        return new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.GONE,
                "Legacy paper and correction preparation is retired; use /cfd/enrollmentPapers");
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
        model.addAttribute("cfd", staffProfiles.cfdForm(id));
        return "AdminCFDEdit";
    }

    @PostMapping("/saveEditedCFD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedCFD(
            @Valid @ModelAttribute("cfd") StaffProfileForm cfd,
            BindingResult bindingResult) {
        staffProfiles.cfdForm(cfd.getId());
        if (bindingResult.hasErrors()) return "AdminCFDEdit";
        try {
            staffProfiles.updateCfd(cfd);
        } catch (StaffProfileService.UsernameAlreadyUsedException ex) {
            bindingResult.rejectValue("username", "duplicate", "Username already exists");
            return "AdminCFDEdit";
        }
        return "redirect:/cfd/getCFDs";
    }


}











