package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;

import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;

import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.repo.VDRepo;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vd")
public class vdController extends BaseController{
    private final serviceUser serviceUser;
    @Autowired
    private InfoConRepo conRepo ;
    @Autowired
    private VDRepo vdRepo;

    @Autowired
    public vdController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @GetMapping("/InfoConcourAdd")
    public String VDInfoConcourAdd(Model model){
        model.addAttribute("infoConcour",new InfoConcour());
        return "InfoConcourAdd";
    }

    @PostMapping("/saveInfoConcour")
    public String saveInfoConcour(Model model , @Valid InfoConcour infoConcour , BindingResult bindingResult, Authentication authentication){
        if (bindingResult.hasErrors()) return "InfoConcourAdd";

        //infoConcour.setDateConcour(new Date());
        conRepo.save(infoConcour);
        return "redirect:/vdPage";
    }

    }











