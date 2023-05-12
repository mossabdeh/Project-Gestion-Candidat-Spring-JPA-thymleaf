package ntic.tlsi.gestiondoctorat2.sec;

import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/*
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
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

    // add repositories for other concrete subclasses of User

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = adminRepo.findByUsername(username);
        if (user == null) {
            user = candidatRepo.findByUsername(username);
            // add code to load the user from other repositories
        }
        if (user == null) {
            user = cfdRepo.findByUsername(username);
        }
        if (user == null) {
            user = enseignantRepo.findByUsername(username);
            if (user == null) {
                user = vdRepo.findByUsername(username);
            }
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (user.getTypeRole() == Role.ADMIN) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if (user.getTypeRole() == Role.CANDIDAT) {
                authorities.add(new SimpleGrantedAuthority("ROLE_CANDIDAT"));
            } // add cases for other roles
            else if (user.getTypeRole() == Role.ENSEIGNANT) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ENSEIGNANT"));
            } else if (user.getTypeRole() == Role.CFD) {
                authorities.add(new SimpleGrantedAuthority("ROLE_CFD"));
            } else if (user.getTypeRole() == Role.VD) {
                authorities.add(new SimpleGrantedAuthority("ROLE_VD"));

                return new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        authorities);
            }
        }

        throw new UsernameNotFoundException("User not found");
    }*//*
    }*/