package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.User;

import java.util.Optional;

public interface Iservice {
    public void InitUsers();

    public void InitInfoC();

     void InitCopie();
     void InitCorrection();
    Optional<User> loadUserByUsername(String username);

}
