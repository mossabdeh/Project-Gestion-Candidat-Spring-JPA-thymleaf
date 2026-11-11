package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.CFD;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.repo.UserAccountRepo;
import ntic.tlsi.gestiondoctorat2.service.Iservice;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminRepositoryScopeTests {

    @Autowired private AdminRepo adminRepo;
    @Autowired private UserAccountRepo userAccountRepo;
    @Autowired private serviceUser users;
    @Autowired private Iservice authenticationService;

    @Test
    void adminQueriesExcludeOtherRoles() {
        Admin admin = createAdmin();
        CFD cfd = createCfd();

        assertTrue(adminRepo.findById(admin.getId()).isPresent());
        assertTrue(adminRepo.findById(cfd.getId()).isEmpty());
        assertTrue(users.getAdmins().stream().anyMatch(a -> a.getId().equals(admin.getId())));
        assertTrue(users.getAdmins().stream().noneMatch(a -> a.getId().equals(cfd.getId())));
        assertTrue(adminRepo.findByNomContains("Scope", PageRequest.of(0, 20))
                .getContent().stream().allMatch(a -> a.getTypeRole() == Role.ADMIN));
    }

    @Test
    void nonAdminIdReturnsNotFoundInsteadOfClassCast() {
        CFD cfd = createCfd();
        ResponseStatusException failure = assertThrows(ResponseStatusException.class,
                () -> users.getAdmin(cfd.getId()));
        assertEquals(HttpStatus.NOT_FOUND, failure.getStatusCode());
    }

    @Test
    void missingAdminReturnsNotFound() {
        ResponseStatusException failure = assertThrows(ResponseStatusException.class,
                () -> users.getAdmin(Long.MAX_VALUE));
        assertEquals(HttpStatus.NOT_FOUND, failure.getStatusCode());
    }

    @Test
    void authenticationStillFindsUsersOfOtherRoles() {
        CFD cfd = createCfd();
        User authenticated = authenticationService.loadUserByUsername(cfd.getUsername()).orElseThrow();
        assertEquals(Role.CFD, authenticated.getTypeRole());
    }

    private Admin createAdmin() {
        Admin admin = new Admin("admin-" + UUID.randomUUID(),
                "initial-password-123", "admin@example.com", "Scope", "Admin",
                Role.ADMIN, new Date());
        adminRepo.saveAndFlush(admin);
        return admin;
    }

    private CFD createCfd() {
        CFD cfd = new CFD("cfd-" + UUID.randomUUID(),
                "initial-password-123", "cfd@example.com", "Scope", "CFD",
                Role.CFD, new Date());
        userAccountRepo.saveAndFlush(cfd);
        return cfd;
    }
}
