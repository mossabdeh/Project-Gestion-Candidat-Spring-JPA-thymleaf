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
    private CandidatRepo candidatRepo;

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
        for (Long candidateId : selectedCandidates) {
            Candidat candidat = candidatRepo.findCandidatById(candidateId);
            if (candidat != null) {
                String code = serviceUser.generateUniqueCode(); // Replace with your code generation logic
                candidat.setCode(code);
                candidatRepo.save(candidat);
            }
        }
        return "vdPage"; // Redirect back to the candidate list page
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
        User vd = vdRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vd Id:" + id));
        model.addAttribute("vd", vd);
        return "AdminVDEdit";
    }

    @PostMapping("/saveEditedVD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedVD(Model model, @Valid VD vd, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "AdminVDEdit";
        }

        // Check if username already exists (excluding the current admin being edited)
        if (vdRepo.existsByUsernameAndIdNot(vd.getUsername(), vd.getId())) {
            bindingResult.rejectValue("username", "error.vd", "Username already exists");
            return "AdminVDEdit";
        }

        // Retrieve the existing admin from the database
        User existingAdmin = vdRepo.findById(vd.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid vd Id:" + vd.getId()));

        // Set the existing password on the edited admin
        vd.setPassword(existingAdmin.getPassword());

        vd.setTypeRole(Role.VD);
        vd.setLogDate(new Date());

        vdRepo.save(vd);
        return "redirect:/vd/getVDs";
    }

}











