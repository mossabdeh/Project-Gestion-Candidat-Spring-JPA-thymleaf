package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class serviceUser {
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
// ------------------------------ ADMIN  CRUD OPERATION -------------------------------------------------
    public Admin addAdmin(Admin admin){
        return adminRepo.save(admin);
    }

    public List<Admin> getAdmins(){
       Iterable<User> users = adminRepo.findAll();
        //return StreamSupport.stream(admins.spliterator(),false).collect(Collectors.toList());
        List<Admin> admins = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Admin) {
                admins.add((Admin) user);
            }
        }
        return admins;
    }

    public Admin getAdmin(Long id){
        return (Admin) adminRepo.findById(id).orElseThrow(()->{
            new RuntimeException();
            return null;
        });
    }

    public Admin deleteAdmin(Long id){
        Admin admin = getAdmin(id);
        adminRepo.delete(admin);
        return admin;
    }

    public Admin editAdmin(Long id , Admin admin){
        Admin adminToEdit = getAdmin(id);
        adminToEdit.setLogDate(admin.getLogDate());
        adminToEdit.setUsername(admin.getUsername());
        adminToEdit.setPassword(admin.getPassword());
        adminToEdit.setNom(admin.getNom());
        adminToEdit.setPrenom(admin.getPrenom());
        adminToEdit.setEmail(admin.getEmail());
        return adminToEdit;
    }

    // ------------------------------ CANDIDAT  CRUD OPERATION -------------------------------------------------
    public Candidat addCandidat(Candidat candidat) {
        return candidatRepo.save(candidat);
    }

    public List<Candidat> getCandidats() {
        Iterable<User> users = candidatRepo.findAll();
        //return StreamSupport.stream(admins.spliterator(),false).collect(Collectors.toList());
        List<Candidat> candidats = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Admin) {
                candidats.add((Candidat) user);
            }
        }
        return candidats;
    }

    public Candidat getCandidat(Long id) {
        return (Candidat) candidatRepo.findById(id).orElseThrow(() -> {
            new RuntimeException();
            return null;
        });
    }

    public Candidat deleteCandidat(Long id) {
        Candidat candidat = getCandidat(id);
        candidatRepo.delete(candidat);
        return candidat;
    }

    public Candidat editCandidat(Long id, Candidat candidat) {
        Candidat candidatToEdit = getCandidat(id);
        candidatToEdit.setDateNaissance(candidat.getDateNaissance());
        candidatToEdit.setUsername(candidat.getUsername());
        candidatToEdit.setPassword(candidatToEdit.getPassword());
        candidatToEdit.setPrenom(candidat.getPrenom());
        candidatToEdit.setNom(candidat.getNom());
        candidatToEdit.setEmail(candidat.getEmail());
        return candidatToEdit;
    }

}
