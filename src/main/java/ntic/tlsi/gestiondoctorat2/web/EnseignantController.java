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
    private CorrectionRepo correctionRepo;


    @Autowired
    public EnseignantController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
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


    @GetMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteEnseignant(final Long id,String keyword,int page){
        enseignantRepo.deleteById(id);
        return "redirect:/enseignant/getEnseignants?page="+page+"&keyword="+keyword;
    }


    @GetMapping("/editEnseignant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editEnseignant(Model model, @RequestParam("id") Long id) {
        Optional<Enseignant> enseignantOptional = enseignantRepo.findEnseignantOptionalById(id);
        Enseignant enseignant = enseignantOptional.orElseThrow(() -> new IllegalArgumentException("Invalid enseignant Id:" + id));
        model.addAttribute("enseignant", enseignant);
        return "AdminEnseignantEdit";
    }


    @PostMapping("/saveEditedEnseignant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedEnseignant(Model model, @Valid Enseignant enseignant, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "AdminEnseignantEdit";
        }

        // Check if username already exists (excluding the current admin being edited)
        if (enseignantRepo.existsByUsernameAndIdNot(enseignant.getUsername(), enseignant.getId())) {
            bindingResult.rejectValue("username", "error.enseignant", "Username already exists");
            return "AdminEnseignantEdit";
        }

        // Retrieve the existing admin from the database
        User existingAdmin = enseignantRepo.findById(enseignant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid enseignant Id:" + enseignant.getId()));

        // Set the existing password on the edited admin
        enseignant.setPassword(existingAdmin.getPassword());

        enseignant.setTypeRole(Role.ENSEIGNANT);



        enseignantRepo.save(enseignant);
        return "redirect:/enseignant/getEnseignants";
    }

    @PostMapping("/saveNotes")
    public String saveNotes(@RequestParam("correctionIds") Long[] correctionIds,
                            @RequestParam("notes") double[] notes,
                            Authentication authentication) {
        String username = authentication.getName();
        Optional<User> enseignant = enseignantRepo.findByUsername(username);
        Long loggedInEnseignantId = enseignant.get().getId();

        // Update the notes for each correction using the provided IDs
        for (int i = 0; i < correctionIds.length; i++) {
            Long correctionId = correctionIds[i];
            double note = notes[i];

            Correction correction = correctionRepo.findByIdAndEnseignantId(correctionId, loggedInEnseignantId);
            correction.setNote(note);
            correctionRepo.save(correction);
        }

        return "redirect:/enseignant/getCorrection";
    }


    @GetMapping("/getCorrection")
    public String getCorrection(Model model,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name = "size", defaultValue = "5") int size,
                                @RequestParam(name = "keyword", defaultValue = "") String keyword,
                                Authentication authentication) {
        String username = authentication.getName();
        Optional<User> enseignant = enseignantRepo.findByUsername(username);
        Long loggedInEnseignantId = enseignant.get().getId();

        Page<Correction> pageCorrections = correctionRepo.findByEnseignantId(loggedInEnseignantId, PageRequest.of(page, size));
        model.addAttribute("corrections", pageCorrections.getContent());
        model.addAttribute("pages", new int[pageCorrections.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "EnseignantCopieCorrection"; // Replace "correctionPage" with the actual name of your correction page view
    }




}












