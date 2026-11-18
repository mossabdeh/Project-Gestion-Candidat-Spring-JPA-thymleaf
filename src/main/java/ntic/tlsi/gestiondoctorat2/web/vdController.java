package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;

import ntic.tlsi.gestiondoctorat2.entities.*;

import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.repo.VDRepo;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vd")
public class vdController extends BaseController{
    private final serviceUser serviceUser;
    @Autowired
    private InfoConRepo conRepo ;
    @Autowired
    private VDRepo vdRepo;
    @Autowired
    private StaffProfileService staffProfiles;
    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.CompetitionPreparationService preparationService;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.EnrollmentAnonymizationService anonymizationService;

    @Autowired
    public vdController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    private Optional<InfoConcour> chosenCompetition(Long competitionId) {
        if (competitionId == null) return conRepo.findTopByOrderByIdDesc();
        return Optional.of(conRepo.findById(competitionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Competition not found")));
    }

    @GetMapping("/InfoConcourAdd")
    public String VDInfoConcourAdd(Model model){
        model.addAttribute("infoConcour",new InfoConcour());
        return "InfoConcourAdd";
    }

    @PostMapping("/saveInfoConcour")
    public String saveInfoConcour(Model model , @Valid InfoConcour infoConcour , BindingResult bindingResult, Authentication authentication){
        if (infoConcour.getRegistrationOpensAt() == null) {
            bindingResult.rejectValue("registrationOpensAt", "required", "Opening date and time required");
        }
        if (infoConcour.getRegistrationClosesAt() == null) {
            bindingResult.rejectValue("registrationClosesAt", "required", "Closing date and time required");
        }
        if (!infoConcour.isRegistrationWindowValid()) {
            bindingResult.reject("invalidWindow", "Registration must open before closing, no later than exam day");
        }
        if (bindingResult.hasErrors()) return "InfoConcourAdd";
        // getting VD name from authentication
        String nameVD = authentication.getName();

        VD vd = vdRepo.findByNom(nameVD);
        // set vd into infoConcour
        infoConcour.setVd(vd);

        conRepo.save(infoConcour);
        return "redirect:/vdPage";
    }

    @GetMapping("/getCandidatsCode")
    public String getCandidatsCode(Model model,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "8") int size,
                                   @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        Page<Candidat> pageCandidats = candidatRepo.findByNomContains(keyword, PageRequest.of(page, size));
       /* for (Candidat candidat : pageCandidats) {
            String code = serviceUser.generateUniqueCode(); // Replace with your code generation logic
            candidat.setCode(code);
            candidatRepo.save(candidat); // Save the updated Candidat object
        }*/
        model.addAttribute("ListCandidats", pageCandidats);
        model.addAttribute("pages", new int[pageCandidats.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "VDCandidatCode";
    }

    @PostMapping("/generateCodes")
    public String generateCodes(@RequestParam("selectedCandidates") List<Long> selectedCandidates) {
        preparationService.assignCodes(selectedCandidates);
        return "redirect:/vd/getCandidatsCode";
    }

    @GetMapping("/enrollmentCodes")
    @PreAuthorize("hasRole('ROLE_VD')")
    public String enrollmentCodes(Model model,
                                  @RequestParam(name = "competitionId", required = false) Long competitionId) {
        model.addAttribute("competitions", conRepo.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "id")));
        chosenCompetition(competitionId).ifPresent(competition -> {
            model.addAttribute("competition", competition);
            model.addAttribute("enrollmentCodes",
                    anonymizationService.listCodes(competition.getId()));
        });
        return "VDEnrollmentCodes";
    }

    @PostMapping("/assignEnrollmentCodes")
    @PreAuthorize("hasRole('ROLE_VD')")
    public String assignEnrollmentCodes(@RequestParam("competitionId") Long competitionId) {
        anonymizationService.assignCodes(competitionId);
        return "redirect:/vd/enrollmentCodes?competitionId=" + competitionId;
    }

    @GetMapping("/getVDs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getVDs(Model model,
                               @RequestParam(name = "page",defaultValue = "0") int page,
                               @RequestParam(name = "size",defaultValue = "5") int size,
                               @RequestParam(name = "keyword",defaultValue = "") String keyword) {
        Page<VD> pageVD=vdRepo.findByNomContains(keyword, PageRequest.of(page,size));
        model.addAttribute("ListVDs",pageVD.getContent());
        model.addAttribute("pages",new int[pageVD.getTotalPages()]);
        model.addAttribute("currentPage",page);
        model.addAttribute("keyword",keyword);

        return "AdminVD";
    }

    @GetMapping("/editVD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editVD(Model model, @RequestParam("id") Long id) {
        model.addAttribute("vd", staffProfiles.viceDeanForm(id));
        return "AdminVDEdit";
    }

    @PostMapping("/saveEditedVD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedVD(
            @Valid @ModelAttribute("vd") StaffProfileForm vd,
            BindingResult bindingResult) {
        staffProfiles.viceDeanForm(vd.getId());
        if (bindingResult.hasErrors()) return "AdminVDEdit";
        try {
            staffProfiles.updateViceDean(vd);
        } catch (StaffProfileService.UsernameAlreadyUsedException ex) {
            bindingResult.rejectValue("username", "duplicate", "Username already exists");
            return "AdminVDEdit";
        }
        return "redirect:/vd/getVDs";
    }


}











