package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;
import ntic.tlsi.gestiondoctorat2.entities.*;

import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.text.DecimalFormat;
import java.util.*;


@Controller
@RequestMapping("/cfd")
public class CfdController extends BaseController{


    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private CFDRepo cfdRepo;
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
   return "redirect:/cfdPage" ;
    }


    @GetMapping("/getCorrectionCopie")
    public String getCorrectionCopie(Model model,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "5") int size,
                                     @RequestParam(name = "keyword", defaultValue = "") String keyword){


        Page<Copie> copiePage = copieRepo.findAll(PageRequest.of(page, size));
        model.addAttribute("copies", copiePage.getContent());
        model.addAttribute("pages", new int[copiePage.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "cfdCheckCorrection"; // Replace "correctionPage" with the actual name of your correction page view
    }

    @GetMapping("/testNotes")
    public String TestNotes(){

        List<Correction> corrections = correctionRepo.findAll();
        double[] notes = new double[] {14, 10, 15, 9, 8, 5, 2, 13.5};
        double note = notes[new Random().nextInt(notes.length)];
        for (Correction correction : corrections){
            if (correction.getNote()== 0 ){
                correction.setNote(note);
                correctionRepo.save(correction);
            }

        }
        return "redirect:/cfd/getCorrectionCopie";
    }

    @PostMapping("/ThirdCorrectionCheck")
    public String thirdCorrectionCheck() {
        List<Copie> copies = copieRepo.findAll();

        for (Copie copie : copies) {
            Correction correction1 = copie.getCorrections().get(0);
            Correction correction2 = copie.getCorrections().get(1);
            double note1 = correction1.getNote();
            double note2 = correction2.getNote();

            if (Math.abs(note1 - note2) >= 3.0 || Math.abs(note2 - note1) >= 3.0) {
                List<Enseignant> enseignants = enseignantRepo.findBySpecialite(copie.getMatier());

                for (Enseignant enseignant : enseignants) {
                    if (enseignant != correction1.getEnseignant() && enseignant != correction2.getEnseignant()) {
                        Correction correction3 = new Correction();
                        correction3.setCopie(copie);
                        correction3.setEnseignant(enseignant);

                        copie.getCorrections().add(correction3);
                        copieRepo.save(copie);
                        correctionRepo.save(correction3);
                        break;  // Stop after assigning the third teacher
                    }
                }
            }
        }

        return "redirect:/cfd/getCorrectionCopie";
    }

    @GetMapping("/ResultatCandidats")
    public String ResultatCandidats(Model model,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "5") int size,
                                    @RequestParam(name = "keyword", defaultValue = "") String keyword) {
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

        return "cfdResultatsCandidats";
    }






    @PostMapping("/CalculResultat")
    public String CalculResultat() {
        List<Candidat> candidatsWithNonNullCode = new ArrayList<>();
        candidatRepo.findAllBy().forEach(candidat -> {
            String code = candidat.getCode();
            if (code != null) {
                candidatsWithNonNullCode.add(candidat);
            }
        });

        for (Candidat candidat : candidatsWithNonNullCode) {
            List<Copie> copies = (List<Copie>) candidat.getCopies();
            double moyMatier1 = calculateAverageNoteForCopies(copies, 0);
            double moyMatier2 = calculateAverageNoteForCopies(copies, 1);
            double moyGeneral = (moyMatier1 + moyMatier2) / 2;

            // Format moyMatier1 and moyMatier2 to have two decimal places
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            String formattedMoyMatier1 = decimalFormat.format(moyMatier1).replace(",", ".");
            String formattedMoyMatier2 = decimalFormat.format(moyMatier2).replace(",", ".");
            String formattedMoyGeneral = decimalFormat.format(moyGeneral).replace(",", ".");

            candidat.setMoyMatier1(Double.parseDouble(formattedMoyMatier1));
            candidat.setMoyMatier2(Double.parseDouble(formattedMoyMatier2));
            candidat.setMoyenneGeneral(Double.parseDouble(formattedMoyGeneral));
            candidatRepo.save(candidat);
        }

        return "redirect:/cfd/ResultatCandidats";
    }

    private double calculateAverageNoteForCopies(List<Copie> copies, int index) {
        double sum = 0.0;
        int count = 0;

        if (index >= 0 && index < copies.size()) {
            Copie copie = copies.get(index);
            for (Correction correction : copie.getCorrections()) {
                sum += correction.getNote();
                count++;
            }
        }

        return count > 0 ? sum / count : 0.0;
    }




    @PostMapping("/SetPostForTopCandidates")
    public String setPostForTopCandidates() {
        List<Candidat> candidatsWithNonNullCode = new ArrayList<>();
        candidatRepo.findAllBy().forEach(candidat -> {
            String code = candidat.getCode();
            if (code != null) {
                candidatsWithNonNullCode.add(candidat);
            }
        });

        // Sort the candidats based on moyGeneral in descending order
        candidatsWithNonNullCode.sort(Comparator.comparingDouble(Candidat::getMoyenneGeneral).reversed());

        // Set post=true for the top three candidats
        for (int i = 0; i < Math.min(candidatsWithNonNullCode.size(), 3); i++) {
            Candidat candidat = candidatsWithNonNullCode.get(i);
            candidat.setGetPoste(true);
            candidatRepo.save(candidat);
        }

        return "redirect:/cfd/ResultatCandidats";
    }


    @GetMapping("/getCFDs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getCFDs(Model model,
                         @RequestParam(name = "page",defaultValue = "0") int page,
                         @RequestParam(name = "size",defaultValue = "5") int size,
                         @RequestParam(name = "keyword",defaultValue = "") String keyword) {
        Page<CFD> pageCFD=cfdRepo.findByNomContains(keyword, PageRequest.of(page,size));
        model.addAttribute("ListCFDs",pageCFD.getContent());
        model.addAttribute("pages",new int[pageCFD.getTotalPages()]);
        model.addAttribute("currentPage",page);
        model.addAttribute("keyword",keyword);

        return "AdminCFD";
    }

    @GetMapping("/editCFD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editCFD(Model model, @RequestParam("id") Long id) {
        User cfd = cfdRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid cfd Id:" + id));
        model.addAttribute("cfd", cfd);
        return "AdminCFDEdit";
    }

    @PostMapping("/saveEditedCFD")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveEditedCFD(Model model, @Valid CFD cfd, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "AdminCFDEdit";
        }

        // Check if username already exists (excluding the current admin being edited)
        if (cfdRepo.existsByUsernameAndIdNot(cfd.getUsername(), cfd.getId())) {
            bindingResult.rejectValue("username", "error.cfd", "Username already exists");
            return "AdminCFDEdit";
        }

        // Retrieve the existing admin from the database
        User existingAdmin = cfdRepo.findById(cfd.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid cfd Id:" + cfd.getId()));

        // Set the existing password on the edited admin
        cfd.setPassword(existingAdmin.getPassword());

        cfd.setTypeRole(Role.CFD);
        cfd.setLogDate(new Date());

        cfdRepo.save(cfd);
        return "redirect:/cfd/getCFDs";
    }


}











