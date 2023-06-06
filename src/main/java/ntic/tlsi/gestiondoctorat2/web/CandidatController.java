package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;

import ntic.tlsi.gestiondoctorat2.entities.*;

import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/candidat")
public class CandidatController extends BaseController{


    private final serviceUser serviceUser;
    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private InfoConRepo conRepo;


    @Autowired
    public CandidatController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @GetMapping("/getConcourInfo")
    public String GetConcourInfo(Model  model){
        List<InfoConcour> concours = conRepo.findAll();
        // return only the last Concour Information
        if (!concours.isEmpty()) {
            InfoConcour lastConcour = concours.get(concours.size() - 1);
            model.addAttribute("lastConcour", lastConcour);
        }
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

    @GetMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteCandidat(final Long id,String keyword,int page){
        candidatRepo.deleteById(id);
        return "redirect:/candidat/getCandidats?page="+page+"&keyword="+keyword;
    }

    @GetMapping("/editCandidat")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editCandidat(Model model, @RequestParam("id") Long id) {
        Optional<Candidat> candidatOptional = candidatRepo.findCandidatOptionalById(id);
        Candidat candidat = candidatOptional.orElseThrow(() -> new IllegalArgumentException("Invalid candidat Id:" + id));
        model.addAttribute("candidat", candidat);
        return "AdminCandidatEdit";
    }


    @PostMapping("/saveEditedCandidat")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedCandidat(Model model, @Valid Candidat candidat, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "AdminCandidatEdit";
        }

        // Check if username already exists (excluding the current admin being edited)
        if (candidatRepo.existsByUsernameAndIdNot(candidat.getUsername(), candidat.getId())) {
            bindingResult.rejectValue("username", "error.candidat", "Username already exists");
            return "AdminCandidatEdit";
        }

        // Retrieve the existing admin from the database
        User existingAdmin = candidatRepo.findById(candidat.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid candidat Id:" + candidat.getId()));

        // Set the existing password on the edited admin
        candidat.setPassword(existingAdmin.getPassword());

        candidat.setTypeRole(Role.CANDIDAT);



        candidatRepo.save(candidat);
        return "redirect:/candidat/getCandidats";
    }


    @GetMapping("/ResultatCandidat")
    public String ResultatCandidats(Model model,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "5") int size,
                                    @RequestParam(name = "keyword", defaultValue = "") String keyword ,
                                    Authentication authentication) {
        String username = authentication.getName();
        Optional<User> MyCandidat = candidatRepo.findByUsername(username);
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
        model.addAttribute("MyCandidat",MyCandidat);

        return "CandidatResultats";
    }

    }












