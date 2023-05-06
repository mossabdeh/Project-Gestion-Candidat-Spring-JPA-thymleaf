package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
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
        Stream.of("prof","doctor","masterr","shifou","maitre yi","zed","shadow")
                .forEach(nameUser ->{
        User ens = new Enseignant(nameUser,nameUser,nameUser+"@gmail.com",nameUser,nameUser, Role.ENSEIGNANT,"Professor","EDL");
        enseignantRepo.save(ens);});

    }
}
