package ntic.tlsi.gestiondoctorat2.service;

import lombok.AllArgsConstructor;
import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.EnseignantDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.VdDTO;
import ntic.tlsi.gestiondoctorat2.repo.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.stream.Stream;

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
    public void InitUsers() {
       //---------------------- Admin -----------------------------------------
        User admin = new Admin("admin","admin","admin@gmail.com"
                ,"admin","admin", Role.ADMIN,new Date());
        adminRepo.save(admin);
        User admin1 = new Admin("admin1","admin1","admin1@gmail.com"
                ,"admin1","admin1", Role.ADMIN,new Date());
        adminRepo.save(admin1);
        //---------------------- VD -----------------------------------------
        User vd = new VD("vd","vd","vd@gmail.com"
                ,"vd","vd", Role.VD,new Date());
        vdRepo.save(vd);
        //---------------------- CFD -----------------------------------------
        User cfd = new CFD("cfd","cfd","cfd@gmail.com"
                ,"cfd","cfd", Role.CFD,new Date());
        cfdRepo.save(cfd);
        //---------------------- Candidats -----------------------------------------
        Random random = new Random();
        for ( int  i = 0;i<7;i++){
            final int index = i;
        Stream.of("mossab","Seif","Islme","Didou","nadjib","Kobi","Ammar","yahia","hakim")
                .forEach(nameUser ->{

        User candidat = new Candidat(nameUser+index,nameUser,nameUser+"@gmail.com",nameUser,nameUser, Role.CANDIDAT
                ,new Date(),null,0.00);

        candidatRepo.save(candidat);
                });}


        //---------------------- Enseingants -----------------------------------------
       /*// Random random = new Random();
        Matier[] specialites = Matier.values();

        Stream.of("edl","svs","algo","tbib","yasou","prof","doc","algo")
                .forEach(nameUser ->{
        User ens = new Enseignant(nameUser,nameUser,nameUser+"@gmail.com",nameUser,nameUser, Role.ENSEIGNANT,"Professor",specialites[random.nextInt(specialites.length)]);
        enseignantRepo.save(ens);});*/

    }




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
    private static final Logger logger = LoggerFactory.getLogger(IserviceImpl.class);


  //  private UserRepo userRepo;
    @Override
    public Optional<User> loadUserByUsername(String username) {
            Optional<User> user = adminRepo.findByUsername(username);
            if (user == null) {
                Optional<User> optionalUser = cfdRepo.findByUsername(username);
                if (optionalUser.isPresent()) {
                    user = optionalUser;
                } else {
                    Optional<VdDTO> optionalVdDTO = vdRepo.findDTOByUsername(username);

                    if (optionalVdDTO.isPresent()) {
                        VD vd = VD.from(optionalVdDTO.get());
                        user = Optional.of(vd);
                    } else {
                        Optional<CandidatDTO> optionalCandidatDTO = candidatRepo.findDTOByUsername(username);
                        if (optionalCandidatDTO.isPresent()) {
                            logger.debug("CandidatDTO --------------: {}", optionalCandidatDTO);
                            Candidat candidat = Candidat.from(optionalCandidatDTO.get());
                            logger.debug("Candidat---------------: {}", candidat);
                            user = Optional.of(candidat);
                        } else {
                            Optional<EnseignantDTO> optionalEnseignantDTO = enseignantRepo.findDTOByUsername(username);
                            if (optionalEnseignantDTO.isPresent()) {
                                Enseignant enseignant = Enseignant.from(optionalEnseignantDTO.get());
                                user = Optional.of(enseignant);
                            }
                        }
                    }
                }
            }
            if (user == null) {
                throw new UsernameNotFoundException(String.format("User %s not found ",username));
            }
            return user;
        }




}

