package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.Iservice;
import ntic.tlsi.gestiondoctorat2.service.IserviceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Date;

@SpringBootApplication
public class GestionDoctorat2Application implements CommandLineRunner {
@Autowired
private IserviceImpl iservice;
    public static void main(String[] args) {
       // ConfigurableApplicationContext configurableApplicationContext =
        SpringApplication.run(GestionDoctorat2Application.class, args);
        /*
        UserRepo adminRepo = configurableApplicationContext.getBean(AdminRepo.class);
        UserRepo cfdRepo = configurableApplicationContext.getBean(CFDRepo.class);
        UserRepo vdRepo = configurableApplicationContext.getBean(VDRepo.class);
        UserRepo candidatRepo = configurableApplicationContext.getBean(CandidatRepo.class);
        UserRepo ensRepo = configurableApplicationContext.getBean(EnseignantRepo.class);

        User admin = new Admin("admin","admin","admin@gmail.com","admin","admin", Role.ADMIN,new Date());
        User vd = new VD("vd","vd","vd@gmail.com","vd","vd", Role.VD,new Date());
        User cfd = new CFD("cfd","cfd","cfd@gmail.com","cfd","cfd", Role.CFD,new Date());
        User candidat = new Candidat("moss","moss","moss@gmail.com","moss","moss", Role.CANDIDAT,new Date(),1233,10.00);
        User ens = new Enseignant("ens","ens","ens@gmail.com","ens","ens", Role.ENSEIGNANT,"Professor","EDL");

        adminRepo.save(admin);
        cfdRepo.save(cfd);
        vdRepo.save(vd);
        candidatRepo.save(candidat);
        ensRepo.save(ens);
    */}

    @Override
    public void run(String... args) throws Exception {
        iservice.InitUsers();
        iservice.InitInfoC();

    }
}
