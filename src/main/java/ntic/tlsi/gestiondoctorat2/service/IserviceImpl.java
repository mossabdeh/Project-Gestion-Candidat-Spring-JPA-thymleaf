package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
public class IserviceImpl implements Iservice {
    @Autowired
     private AdminRepo adminRepo;
    @Autowired
    private CFDRepo cfdRepo;
    @Autowired
    private VDRepo vdRepo;
    @Autowired
    private CandidatRepo candidatRepo;
    @Autowired
    private EnseignantRepo enseignantRepo;
    @Autowired
    private InfoConRepo conRepo;
    @Autowired
    private CopieRepo copieRepo;

    @Override
    public void InitUsers() {
        //---------------------- Admin -----------------------------------------
        User admin = new Admin("admin","admin","admin@gmail.com"
                ,"admin","admin", Role.ADMIN,new Date());
        adminRepo.save(admin);
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
        for (int i = 0;i<7;i++){
        Stream.of("Mossab","Seif","Islme","Didou","nadjib","Kobi","Ammar","yahia","hakim")
                .forEach(nameUser ->{

        User candidat = new Candidat(nameUser,nameUser,nameUser+"@gmail.com",nameUser,nameUser, Role.CANDIDAT
                ,new Date(),random.nextInt(1001) + 1000,10.00);

        candidatRepo.save(candidat);
                });}


        //---------------------- Enseingants -----------------------------------------
        Matier[] specialites = Matier.values();

        Stream.of("prof","doctor","master","shifou","maitre yi","zed","shadow")
                .forEach(nameUser ->{
        User ens = new Enseignant(nameUser,nameUser,nameUser+"@gmail.com",nameUser,nameUser, Role.ENSEIGNANT,"Professor",specialites[random.nextInt(specialites.length)]);
        enseignantRepo.save(ens);});

    }




    @Override
    public void InitInfoC() {
        VD vd = vdRepo.findByNom("vd");
        InfoConcour concour = new InfoConcour(3,"TLSI",Matier.ALGO,Matier.EDL,new Date());
        List<InfoConcour> concours = Arrays.asList(concour);
        vd.setConcours(concours);
        vd.setLogDate(new Date());
        vdRepo.save(vd);
        // if we delete VD all his infos Concours deleted
        //vdRepo.delete(vd);



    }

    @Override
    public void InitCopie() {
          /* final Logger LOGGER = Logger.getLogger(IserviceImpl.class.getName());
         candidatRepo.findAllBy().forEach(c ->{
             LOGGER.info("Candidat: " + c.getNom());
         });*/
        candidatRepo.findAllBy().forEach(candidat -> {
            Copie copie1 = new Copie(Matier.ALGO,candidat);
            Copie copie2 = new Copie(Matier.EDL,candidat);
            //Copie copie3 = new Copie(Matier.EDL,candidat);
            List<Copie>  copies = Arrays.asList(copie1,copie2);
            candidat.setCopies(copies);
            candidatRepo.save(candidat);

        });
       //Optional<Candidat> candidatOptional  =  candidatRepo.findById(4L).map(u -> (Candidat)u);

        //candidatOptional.ifPresent(candidat -> {





    }
}
