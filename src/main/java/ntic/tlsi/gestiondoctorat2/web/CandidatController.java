package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/candidat")
public class CandidatController extends BaseController{


    private final serviceUser serviceUser;
    @Autowired
    private CandidatRepo candidatRepo;


    @Autowired
    public CandidatController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @PostMapping
    public ResponseEntity<CandidatDTO> addCandidat(@RequestBody final CandidatDTO candidatDTO){
        Candidat candidat = serviceUser.addCandidat(Candidat.from(candidatDTO));
        return new ResponseEntity<>(CandidatDTO.from(candidat), HttpStatus.OK);
    }

    @GetMapping("/getCandidats")
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

    @GetMapping("/CandidatAdd")
    public String AdminCandidatAdd(Model model){
        model.addAttribute("candidat",new Candidat());
        return "AdminCandidatAdd";
    }

    @PostMapping("/saveCandidat")
    public String saveCandidat(Model model , @Valid Candidat candidat , BindingResult bindingResult){
        if (bindingResult.hasErrors()) return "AdminCandidatAdd";
        candidat.setTypeRole(Role.CANDIDAT);
        candidat.setDateNaissance(new Date());
        candidatRepo.save(candidat);
        return "redirect:/candidat/getCandidats";
    }
  /* @GetMapping(value = "{id}")
    public ResponseEntity<CandidatDTO> getCandidat(@PathVariable final Long id){
        Candidat  candidat = serviceUser.getCandidat(id);
        return new ResponseEntity<>(CandidatDTO.from(candidat),HttpStatus.OK);
    }*/

    @GetMapping("/delete")
    public String deleteCandidat(final Long id,String keyword,int page){
        candidatRepo.deleteById(id);
        return "redirect:/candidat/getCandidats?page="+page+"&keyword="+keyword;
    }


    @GetMapping("/editCandidat")
    public String editAdmin(Model model,Long id,String keyword,int page){

        Candidat editCandidat = candidatRepo.findCandidatById(id);
        editCandidat.setTypeRole(Role.CANDIDAT);
        editCandidat.setDateNaissance(new Date());
        model.addAttribute("candidat",editCandidat);
        model.addAttribute("page",page);
        model.addAttribute("keyword",keyword);

        return "AdminCandidatEdit";

    }



    }












