package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;

import ntic.tlsi.gestiondoctorat2.entities.*;

import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/candidat")
public class CandidatController extends BaseController{


    private final CandidatRepo candidatRepo;
    private final InfoConRepo conRepo;
    private final CandidatProfileService profileService;

    public CandidatController(CandidatRepo candidatRepo, InfoConRepo conRepo,
                              CandidatProfileService profileService) {
        this.candidatRepo = candidatRepo;
        this.conRepo = conRepo;
        this.profileService = profileService;
    }

    @GetMapping("/getConcourInfo")
    public String GetConcourInfo(Model  model){
        conRepo.findTopByOrderByIdDesc()
                .ifPresent(concours -> model.addAttribute("lastConcour", concours));
        return "CandidatConcourInfo";
    }


    @GetMapping("/getCandidats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getCandidats(Model model,
                            @RequestParam(name = "page",defaultValue = "0") int page,
                            @RequestParam(name = "size",defaultValue = "5") int size,
                            @RequestParam(name = "keyword",defaultValue = "") String keyword) {
        Page<Candidat> pageCandidats=candidatRepo.findByNomContains(keyword, PageRequest.of(page,size));
        model.addAttribute("ListCandidats",pageCandidats.getContent());
        model.addAttribute("pages",new int[pageCandidats.getTotalPages()]);
        model.addAttribute("currentPage",page);
        model.addAttribute("keyword",keyword);

        return "AdminCandidat";
    }

    @GetMapping("/test")
    public String test(Authentication authentication,Model model){
        model.addAttribute("username",authentication.getName());
        return "Test";

    }

    @GetMapping("/CandidatAdd")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String AdminCandidatAdd(Model model){
        model.addAttribute("candidat",new Candidat());
        return "AdminCandidatAdd";
    }

    @PostMapping("/saveCandidat")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveCandidat(Model model , @Valid Candidat candidat , BindingResult bindingResult){
        if (bindingResult.hasErrors()) return "AdminCandidatAdd";
        // Check if username already exists
        if (candidatRepo.existsByUsername(candidat.getUsername())) {
            bindingResult.rejectValue("username", "error.candidat", "Username already exists");
            return "AdminCandidatAdd";
        }
        candidat.setTypeRole(Role.CANDIDAT);
       // candidat.setDateNaissance(new Date());
        // Set the password as the same value as the username
        //candidat.setPassword(candidat.getUsername());
        candidatRepo.save(candidat);
        return "redirect:/candidat/getCandidats";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteCandidat(@RequestParam("id") Long id,
                                @RequestParam(name = "keyword", defaultValue = "") String keyword,
                                @RequestParam(name = "page", defaultValue = "0") int page) {
        // Only delete an account belonging to this role, never an account with
        // the same ID from another user type.
        Candidat account = candidatRepo.findCandidatOptionalById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Candidat not found"));
        candidatRepo.delete(account);
        return "redirect:/candidat/getCandidats?page=" + page + "&keyword=" +
                org.springframework.web.util.UriUtils.encodeQueryParam(keyword, java.nio.charset.StandardCharsets.UTF_8);
    }

    @GetMapping("/editCandidat")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editCandidat(Model model, @RequestParam("id") Long id,
                               @RequestParam(name = "page", required = false) Integer page,
                               @RequestParam(name = "keyword", required = false) String keyword) {
        Candidat existing = candidatRepo.findCandidatOptionalById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Candidate not found"));
        model.addAttribute("candidat", CandidatProfileForm.from(existing));
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "AdminCandidatEdit";
    }

    @PostMapping("/saveEditedCandidat")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedCandidat(
            @Valid @ModelAttribute("candidat") CandidatProfileForm candidat,
            BindingResult bindingResult,
            Model model,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "keyword", required = false) String keyword) {
        if (candidat.getId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Candidate ID is required");
        }
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        if (bindingResult.hasErrors()) {
            return "AdminCandidatEdit";
        }
        try {
            profileService.updateProfile(candidat);
        } catch (CandidatProfileService.UsernameAlreadyUsedException ex) {
            bindingResult.rejectValue("username", "error.candidat", "Username already exists");
            return "AdminCandidatEdit";
        }

        if (page == null && (keyword == null || keyword.isEmpty())) {
            return "redirect:/candidat/getCandidats";
        }
        String encodedKeyword = org.springframework.web.util.UriUtils.encodeQueryParam(
                keyword == null ? "" : keyword, java.nio.charset.StandardCharsets.UTF_8);
        return "redirect:/candidat/getCandidats?page=" + (page == null ? 0 : page)
                + "&keyword=" + encodedKeyword;
    }

    @GetMapping("/ResultatCandidat")
    public String ResultatCandidats(Model model, Authentication authentication) {
        // This view only presents the signed-in candidate's own result.
        // Do not query or attach results belonging to other candidates.
        Candidat currentCandidate = candidatRepo.findCandidatByUsername(authentication.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Candidate account not found"));

        // Preserve the existing Thymeleaf contract until the follow-up
        // subject-ordered results DTO/UI migration.
        model.addAttribute("MyCandidat", Optional.of(currentCandidate));
        return "CandidatResultats";
    }
    }












