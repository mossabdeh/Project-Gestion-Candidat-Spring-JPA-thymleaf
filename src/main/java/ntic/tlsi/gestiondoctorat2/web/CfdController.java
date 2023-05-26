package ntic.tlsi.gestiondoctorat2.web;


import ntic.tlsi.gestiondoctorat2.entities.*;

import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



import java.util.List;


@Controller
@RequestMapping("/cfd")
public class CfdController extends BaseController{


    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private CopieRepo copieRepo;
    @Autowired
    private InfoConRepo conRepo;
    @Autowired
    private EnseignantRepo enseignantRepo;
    @Autowired
    private CorrectionRepo correctionRepo;



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


    @GetMapping("/InitCorrection")
    public String InitCorrection(){
        List<Enseignant> enseignants = enseignantRepo.findAllBy();
        copieRepo.findAll().forEach(copie -> {
            Enseignant enseignant1 = null;
            Enseignant enseignant2 = null;
            Enseignant enseignant3 = null;

            // Find two teachers with the same specialty as the copy
            for (Enseignant enseignant : enseignants) {
                if (enseignant.getSpecialite() == copie.getMatier()) {
                    if (enseignant1 == null) {
                        enseignant1 = enseignant;
                    } else if (enseignant2 == null) {
                        enseignant2 = enseignant;
                        break;
                    }
                }
            }
            // Create and save corrections for each teacher
            Correction correction1 = new Correction();
            correction1.setCopie(copie);
            correction1.setEnseignant(enseignant1);
           // correction1.setNote(note1);
            correctionRepo.save(correction1);

            Correction correction2 = new Correction();
            correction2.setCopie(copie);
            correction2.setEnseignant(enseignant2);
           // correction2.setNote(note2);
            correctionRepo.save(correction2);


        });
   return "cfdPage" ;
    }

    }











