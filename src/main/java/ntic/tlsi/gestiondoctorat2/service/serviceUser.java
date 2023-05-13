package ntic.tlsi.gestiondoctorat2.service;

import jakarta.transaction.Transactional;
import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Transactional
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
            if (user instanceof Candidat) {
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

    @Transactional
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


    // ------------------------------ CFD  CRUD OPERATION -------------------------------------------------
    public CFD addCFD(CFD cfd){
        return cfdRepo.save(cfd);
    }

    public List<CFD> getCFDs(){
        Iterable<User> users = cfdRepo.findAll();
        //return StreamSupport.stream(admins.spliterator(),false).collect(Collectors.toList());
        List<CFD> cfds = new ArrayList<>();
        for (User user : users) {
            if (user instanceof CFD) {
                cfds.add((CFD) user);
            }
        }
        return cfds;
    }

    public CFD getCFD(Long id){
        return (CFD) cfdRepo.findById(id).orElseThrow(()->{
            new RuntimeException();
            return null;
        });
    }

    public CFD deleteCfd(Long id){
        CFD cfd = getCFD(id);
        cfdRepo.delete(cfd);
        return cfd;
    }

    @Transactional
    public CFD editCFD(Long id , CFD cfd){
        CFD cfdToEdit = getCFD(id);
        cfdToEdit.setLogDate(cfd.getLogDate());
        cfdToEdit.setUsername(cfd.getUsername());
        cfdToEdit.setPassword(cfd.getPassword());
        cfdToEdit.setNom(cfd.getNom());
        cfdToEdit.setPrenom(cfd.getPrenom());
        cfdToEdit.setEmail(cfd.getEmail());
        return cfdToEdit;
    }
    // ------------------------------ VD  CRUD OPERATION -------------------------------------------------
    public VD addVD(VD vd){
        return vdRepo.save(vd);
    }

    public List<VD> getVDs(){
        Iterable<User> users = vdRepo.findAll();
        //return StreamSupport.stream(admins.spliterator(),false).collect(Collectors.toList());
        List<VD> vds = new ArrayList<>();
        for (User user : users) {
            if (user instanceof VD) {
                vds.add((VD) user);
            }
        }
        return vds;
    }

    public VD getVD(Long id){
        return (VD) vdRepo.findById(id).orElseThrow(()->{
            new RuntimeException();
            return null;
        });
    }

    public VD deleteVD(Long id){
        VD vd = getVD(id);
        vdRepo.delete(vd);
        return vd;
    }

    @Transactional
    public VD editVD(Long id , VD vd){
        VD vdToEdit = getVD(id);
        vdToEdit.setLogDate(vd.getLogDate());
        vdToEdit.setUsername(vd.getUsername());
        vdToEdit.setPassword(vd.getPassword());
        vdToEdit.setNom(vd.getNom());
        vdToEdit.setPrenom(vd.getPrenom());
        vdToEdit.setEmail(vd.getEmail());
        return vdToEdit;
    }
    // ------------------------------ ENSEIGNANT  CRUD OPERATION -------------------------------------------------
    public Enseignant addEnseignant(Enseignant enseignant){
        return enseignantRepo.save(enseignant);
    }

    public List<Enseignant> getEnseignants(){
        Iterable<User> users = enseignantRepo.findAll();

        List<Enseignant> enseignants = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Enseignant) {
                enseignants.add((Enseignant) user);
            }
        }
        return enseignants;
    }

    public Enseignant getEnseignant(Long id){
        return (Enseignant) enseignantRepo.findById(id).orElseThrow(()->{
            new RuntimeException();
            return null;
        });
    }

    public Enseignant deleteEnseignant(Long id){
        Enseignant enseignant = getEnseignant(id);
        enseignantRepo.delete(enseignant);
        return enseignant;
    }

    @Transactional
    public Enseignant editEnseignant(Long id , Enseignant enseignant){
        Enseignant enseignantToEdit = getEnseignant(id);
        enseignantToEdit.setSpecialite(enseignant.getSpecialite());
        enseignantToEdit.setGrade(enseignant.getGrade());
        enseignantToEdit.setUsername(enseignant.getUsername());
        enseignantToEdit.setPassword(enseignant.getPassword());
        enseignantToEdit.setNom(enseignant.getNom());
        enseignantToEdit.setPrenom(enseignant.getPrenom());
        enseignantToEdit.setEmail(enseignant.getEmail());
        return enseignantToEdit;
    }

}
