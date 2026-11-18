package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;
import ntic.tlsi.gestiondoctorat2.entities.*;

import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.CorrectionRepo;
import ntic.tlsi.gestiondoctorat2.repo.EnseignantRepo;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/enseignant")
public class EnseignantController extends BaseController{


    private final serviceUser serviceUser;
    @Autowired
    private EnseignantRepo enseignantRepo;
    @Autowired
    private StaffProfileService staffProfiles;
    @Autowired
    private CorrectionRepo correctionRepo;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.repo.InfoConRepo infoConRepo;
    @Autowired
    private ntic.tlsi.gestiondoctorat2.service.EnrollmentGradingService enrollmentGrading;


    @Autowired
    public EnseignantController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }



    @GetMapping("/enrollmentGrades")
    @PreAuthorize("hasRole('ROLE_ENSEIGNANT')")
    public String enrollmentGrades(Model model, Authentication authentication) {
        model.addAttribute("assignedGrades", enrollmentGrading.assignments(authentication.getName()));
        return "EnseignantEnrollmentGrades";
    }

    @PostMapping("/saveEnrollmentGrade")
    @PreAuthorize("hasRole('ROLE_ENSEIGNANT')")
    public String saveEnrollmentGrade(@RequestParam("correctionId") Long correctionId,
                                      @RequestParam("grade") double grade,
                                      Authentication authentication) {
        enrollmentGrading.recordGrade(correctionId, authentication.getName(), grade);
        return "redirect:/enseignant/enrollmentGrades";
    }

    @GetMapping("/getEnseignants")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getEnseignants(Model model,
                            @RequestParam(name = "page",defaultValue = "0") int page,
                            @RequestParam(name = "size",defaultValue = "5") int size,
                            @RequestParam(name = "keyword",defaultValue = "") String keyword) {
        Page<Enseignant> pageEnseignants=enseignantRepo.findByNomContains(keyword, PageRequest.of(page,size));
        model.addAttribute("ListEnseignants",pageEnseignants.getContent());
        model.addAttribute("pages",new int[pageEnseignants.getTotalPages()]);
        model.addAttribute("currentPage",page);
        model.addAttribute("keyword",keyword);

        return "AdminEnseignant";
    }

    @GetMapping("/EnseignantAdd")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String AdminEnseignantAdd(Model model){
        model.addAttribute("enseignant",new Enseignant());
        return "AdminEnseignantAdd";
    }

    @PostMapping("/saveEnseignant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEnseignant(Model model , @Valid Enseignant enseignant , BindingResult bindingResult){
        if (bindingResult.hasErrors()) return "AdminEnseignantAdd";
        // Check if username already exists
        if (enseignantRepo.existsByUsername(enseignant.getUsername())) {
            bindingResult.rejectValue("username", "error.enseignant", "Username already exists");
            return "AdminEnseignantAdd";
        }
        enseignant.setTypeRole(Role.ENSEIGNANT);
        // candidat.setDateNaissance(new Date());
        // Set the password as the same value as the username
        //enseignant.setPassword(enseignant.getUsername());
        enseignantRepo.save(enseignant);
        return "redirect:/enseignant/getEnseignants";
    }


    @PostMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteEnseignant(@RequestParam("id") Long id,
                                @RequestParam(name = "keyword", defaultValue = "") String keyword,
                                @RequestParam(name = "page", defaultValue = "0") int page) {
        // Only delete an account belonging to this role, never an account with
        // the same ID from another user type.
        Enseignant account = enseignantRepo.findEnseignantOptionalById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Enseignant not found"));
        enseignantRepo.delete(account);
        return "redirect:/enseignant/getEnseignants?page=" + page + "&keyword=" +
                org.springframework.web.util.UriUtils.encodeQueryParam(keyword, java.nio.charset.StandardCharsets.UTF_8);
    }


    @GetMapping("/editEnseignant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editEnseignant(Model model, @RequestParam("id") Long id) {
        model.addAttribute("enseignant", staffProfiles.teacherForm(id));
        return "AdminEnseignantEdit";
    }

    @PostMapping("/saveEditedEnseignant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedEnseignant(
            @Valid @ModelAttribute("enseignant") TeacherProfileForm enseignant,
            BindingResult bindingResult) {
        // Check role and ID even when submitted fields fail validation.
        staffProfiles.teacherForm(enseignant.getId());
        if (bindingResult.hasErrors()) return "AdminEnseignantEdit";
        try {
            staffProfiles.updateTeacher(enseignant);
        } catch (StaffProfileService.UsernameAlreadyUsedException ex) {
            bindingResult.rejectValue("username", "duplicate", "Username already exists");
            return "AdminEnseignantEdit";
        }
        return "redirect:/enseignant/getEnseignants";
    }

    /**
     * Retired legacy endpoint: never write Candidate-wide grades here.
     * Existing legacy records remain in storage, but new grading must use
     * enrollment-scoped assignments.
     */
    @PostMapping("/saveNotes")
    @PreAuthorize("hasRole('ROLE_ENSEIGNANT')")
    public String saveNotes() {
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.GONE,
                "Legacy grading is retired; use /enseignant/enrollmentGrades");
    }

    @GetMapping("/getCorrection")
    @PreAuthorize("hasRole('ROLE_ENSEIGNANT')")
    public String getCorrection() {
        return "redirect:/enseignant/enrollmentGrades";
    }


}












