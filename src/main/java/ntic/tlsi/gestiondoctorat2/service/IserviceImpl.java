package ntic.tlsi.gestiondoctorat2.service;

import lombok.AllArgsConstructor;
import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@AllArgsConstructor
public class IserviceImpl implements Iservice {

     private AdminRepo adminRepo;

    private CFDRepo cfdRepo;

    private VDRepo vdRepo;

    private CandidatRepo candidatRepo;

    private EnseignantRepo enseignantRepo;

    private InfoConRepo conRepo;

    private CopieRepo copieRepo;

    private CorrectionRepo correctionRepo;


    @Override
    public void InitInfoC() {
        VD vd = vdRepo.findByNom("vd");
        InfoConcour concour = new InfoConcour(3,"TLSI",Matier.ALGO,Matier.EDL,new Date());
        List<InfoConcour> concours = Arrays.asList(concour);
        vd.setConcours(concours);
        concour.setVd(vd);
        conRepo.save(concour);
        // if we delete VD all his infos Concours deleted
        //vdRepo.delete(vd);



    }

    @Override
    public void InitCopie() {

        candidatRepo.findAllBy().forEach(candidat -> {
            Copie copie1 = new Copie(Matier.ALGO,candidat);
            Copie copie2 = new Copie(Matier.EDL,candidat);
            List<Copie>  copies = Arrays.asList(copie1,copie2);
            candidat.setCopies(copies);
            copie1.setCandidat(candidat);
            copie2.setCandidat(candidat);
            //candidatRepo.save(candidat);
            copieRepo.save(copie1);
            copieRepo.save(copie2);

        });





    }

    @Override

    public void InitCorrection() {
        List<Enseignant> enseignants = enseignantRepo.findAllBy();
        double[] notes = new double[] {17, 10, 15, 9, 8, 5, 2, 13.5};

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

            // If there are not two teachers with the same specialty as the copy, skip the correction
            if (enseignant1 == null || enseignant2 == null) {
                return;
            }

            // Assign two random notes to the first two teachers
            double note1 = notes[new Random().nextInt(notes.length)];
            double note2 = notes[new Random().nextInt(notes.length)];

            // If the difference between the two notes is more than 3, assign a third teacher
            if ((Math.abs(note1 - note2) >= 3.0) || (Math.abs(note2 - note1) >= 3.0)) {
                for (Enseignant enseignant : enseignants) {
                    if (enseignant.getSpecialite() == copie.getMatier() && enseignant != enseignant1 && enseignant != enseignant2) {
                        enseignant3 = enseignant;
                        break;
                    }
                }
            }

            // Create and save corrections for each teacher
            Correction correction1 = new Correction();
            correction1.setCopie(copie);
            correction1.setEnseignant(enseignant1);
            correction1.setNote(note1);
            correctionRepo.save(correction1);

            Correction correction2 = new Correction();
            correction2.setCopie(copie);
            correction2.setEnseignant(enseignant2);
            correction2.setNote(note2);
            correctionRepo.save(correction2);

            if (enseignant3 != null) {
                Correction correction3 = new Correction();
                correction3.setCopie(copie);
                correction3.setEnseignant(enseignant3);
                correction3.setNote(notes[new Random().nextInt(notes.length)]);
                correctionRepo.save(correction3);
            }
        });
    }


  //  private UserRepo userRepo;
    @Override
    public Optional<User> loadUserByUsername(String username) {
        // Load the stored hash directly. Mapping through DTOs can lose or rehash it.
        Optional<User> user = adminRepo.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("User %s not found", username));
        }
        return user;
    }

}
