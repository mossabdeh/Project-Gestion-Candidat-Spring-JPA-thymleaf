package ntic.tlsi.gestiondoctorat2.web;

import lombok.AllArgsConstructor;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.sec.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/")
    public String homePage() {
        return "HomePage"; // index.html is your home page template file
    }

    @GetMapping("/adminPage")
    public String AdminPage(){
        return"AdminPage";
    }

    @GetMapping("/candidatPage")
    public String CandidatPage( Authentication authentication, Model model){
        String username = authentication.getName();
        Optional<User> candidat = candidatRepo.findByUsername(username);
        model.addAttribute("candidat", candidat);
        return"CandidatPage";}

    }
/*
    @GetMapping("/loginPage")
    public String login(){
        return "Login";
    }*/



