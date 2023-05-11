package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import ntic.tlsi.gestiondoctorat2.entities.Enseignant;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.EnseignantRepo;
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

@Controller
@RequestMapping("/enseignant")
public class EnseignantController extends BaseController{


    private final serviceUser serviceUser;
    @Autowired
    private EnseignantRepo enseignantRepo;


    @Autowired
    public EnseignantController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }



    @GetMapping("/getEnseignants")
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
    public String AdminEnseignantAdd(Model model){
        model.addAttribute("enseignant",new Enseignant());
        return "AdminEnseignantAdd";
    }

    @PostMapping("/saveEnseignant")
    public String saveEnseignant(Model model , @Valid Enseignant enseignant , BindingResult bindingResult){
        if (bindingResult.hasErrors()) return "AdminEnseignantAdd";
        enseignant.setTypeRole(Role.ENSEIGNANT);

        enseignantRepo.save(enseignant);
        return "redirect:/enseignant/getEnseignants";
    }

    @GetMapping("/delete")
    public String deleteEnseignant(final Long id,String keyword,int page){
        enseignantRepo.deleteById(id);
        return "redirect:/enseignant/getEnseignants?page="+page+"&keyword="+keyword;
    }


    @GetMapping("/editEnseignant")
    public String editEnseignant(Model model,Long id,String keyword,int page){

        Enseignant editEnseignant = enseignantRepo.findEnseignantById(id);
        editEnseignant.setTypeRole(Role.ENSEIGNANT);

        model.addAttribute("enseignant",editEnseignant);
        model.addAttribute("page",page);
        model.addAttribute("keyword",keyword);

        return "AdminEnseignantEdit";

    }



    }












