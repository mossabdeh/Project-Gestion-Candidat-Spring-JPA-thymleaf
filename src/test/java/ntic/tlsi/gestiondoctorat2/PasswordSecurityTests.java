package ntic.tlsi.gestiondoctorat2;

import com.fasterxml.jackson.databind.ObjectMapper;
import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.entities.DTO.*;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PasswordSecurityTests {

    @Autowired private ObjectMapper mapper;
    @Autowired private AdminRepo adminRepo;
    @Autowired private serviceUser users;
    @Autowired private PasswordEncoder encoder;

    @Test
    void apiResponsesNeverContainPasswords() throws Exception {
        Admin admin = new Admin("example", "secret", "a@example.com", "A", "B", Role.ADMIN, new Date());
        admin.setId(1L);
        admin.setPassword("$2a$10$storedHash");

        assertFalse(mapper.readTree(mapper.writeValueAsString(admin)).has("password"));
        assertFalse(mapper.readTree(mapper.writeValueAsString(UserDTO.from(admin))).has("password"));
        assertFalse(mapper.readTree(mapper.writeValueAsString(AdminDTO.from(admin))).has("password"));

        Candidat candidate = new Candidat();
        candidate.setPassword("$2a$10$anotherHash");
        assertFalse(mapper.readTree(mapper.writeValueAsString(CandidatDTO.from(candidate))).has("password"));

        CFD cfd = new CFD();
        cfd.setPassword("$2a$10$anotherHash");
        assertFalse(mapper.readTree(mapper.writeValueAsString(CfdDTO.from(cfd))).has("password"));

        VD vd = new VD();
        vd.setPassword("$2a$10$anotherHash");
        assertFalse(mapper.readTree(mapper.writeValueAsString(VdDTO.from(vd))).has("password"));

        Enseignant teacher = new Enseignant();
        teacher.setPassword("$2a$10$anotherHash");
        assertFalse(mapper.readTree(mapper.writeValueAsString(EnseignantDTO.from(teacher))).has("password"));
    }

    @Test
    void passwordCanStillBeSubmittedInRequestDto() throws Exception {
        AdminDTO request = mapper.readValue("{\"username\":\"test\",\"password\":\"new-secret\"}", AdminDTO.class);
        assertEquals("new-secret", request.getPassword());
        assertFalse(mapper.readTree(mapper.writeValueAsString(request)).has("password"));
    }

    @Test
    void newAccountsAreHashedExactlyOnce() {
        Admin admin = createAdmin("start-password");
        assertTrue(encoder.matches("start-password", admin.getPassword()));
        assertFalse(encoder.matches("start-password", encoder.encode(admin.getPassword())));
    }

    @Test
    void profileEditsKeepTheExistingPassword() {
        Admin admin = createAdmin("keep-password");
        String hash = admin.getPassword();
        Admin changes = new Admin();
        changes.setUsername(admin.getUsername());
        changes.setPassword(" ");
        changes.setNom("Updated");
        users.editAdmin(admin.getId(), changes);
        adminRepo.flush();

        assertEquals(hash, admin.getPassword());
        assertTrue(encoder.matches("keep-password", admin.getPassword()));
    }

    @Test
    void passwordChangesAreHashedAndWork() {
        Admin admin = createAdmin("old-password");
        Admin changes = new Admin();
        changes.setUsername(admin.getUsername());
        changes.setPassword("new-password");
        users.editAdmin(admin.getId(), changes);
        adminRepo.flush();

        assertTrue(encoder.matches("new-password", admin.getPassword()));
        assertFalse(encoder.matches("old-password", admin.getPassword()));
    }

    private Admin createAdmin(String password) {
        Admin admin = new Admin("user-" + UUID.randomUUID(), password, "a@example.com",
                "Given", "Family", Role.ADMIN, new Date());
        adminRepo.saveAndFlush(admin);
        return admin;
    }
}
