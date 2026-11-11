package ntic.tlsi.gestiondoctorat2.web;

import lombok.AllArgsConstructor;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.EnseignantRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.sec.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller

public class BaseController {

    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private EnseignantRepo enseignantRepo;
    @Autowired
    private InfoConRepo competitionRepo;

    @GetMapping("/")
    public String homePage() {
        return "HomePage"; // index.html is your home page template file
    }

    @GetMapping("/adminPage")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String AdminPage(){
        return"AdminPage";
    }
    @GetMapping("/cfdPage")
    public String CFDPage(){
        return"cfdPage";
    }
    @GetMapping("/candidatPage")
    @PreAuthorize("hasRole('ROLE_CANDIDAT')")
    public String CandidatPage(Authentication authentication, Model model) {
        Candidat candidat = candidatRepo.findCandidatByUsername(authentication.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Candidate account not found"));
        model.addAttribute("candidat", candidat);
        model.addAttribute("latestConcour", competitionRepo.findTopByOrderByIdDesc().orElse(null));
        return "CandidatPage";
    }

    @GetMapping("/vdPage")
    public String VDPage(){
        return"vdPage";
    }

    @GetMapping("/enseignantPage")
    public String EnseignantPage( Authentication authentication, Model model){
        String username = authentication.getName();
        Optional<User> enseignant = enseignantRepo.findByUsername(username);
        model.addAttribute("enseignant", enseignant);
        return"enseignantPage";}


}
/*
    @GetMapping("/loginPage")
    public String login(){
        return "Login";
    }*/



