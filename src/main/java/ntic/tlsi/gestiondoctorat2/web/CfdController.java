package ntic.tlsi.gestiondoctorat2.web;


import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CfdDTO;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.CopieRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cfd")
public class CfdController extends BaseController{
    private final serviceUser serviceUser;

    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private CopieRepo copieRepo;
    @Autowired
    private InfoConRepo conRepo;

    @Autowired
    public CfdController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @GetMapping("/initCopie")
    public String initializeCopies() {
        List<InfoConcour> concours = conRepo.findAll();
        if (concours.isEmpty()) {
            return "No concour information found.";
        }
        InfoConcour lastConcour = concours.get(concours.size() - 1);

        // setting copie for the lastConcour 
        candidatRepo.findAllBy().forEach(candidat -> {
            if (candidat.getCode() != null) {
                Copie copie1 = new Copie(lastConcour.getMatier1(), candidat);
                Copie copie2 = new Copie(lastConcour.getMatier2(), candidat);
                candidat.getCopies().add(copie1); // Add copie1 to the collection
                candidat.getCopies().add(copie2); // Add copie2 to the collection
                copie1.setCandidat(candidat);
                copie2.setCandidat(candidat);
                copieRepo.save(copie1);
                copieRepo.save(copie2);
            }
            candidatRepo.save(candidat); // Save the Candidat entity
        });

        return "cfdPage";
    }

    }











