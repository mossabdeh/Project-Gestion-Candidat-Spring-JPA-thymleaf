package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "staff-edit-admin", roles = "ADMIN")
class StaffProfileEditingTests {
    @Autowired MockMvc mvc;
    @Autowired UserAccountRepo accounts;
    @Autowired EnseignantRepo teachers;
    @Autowired CFDRepo cfds;
    @Autowired VDRepo viceDeans;
    @Autowired InfoConRepo competitions;

    @Test
    void teacherEditPreservesCredentialsRoleAndExistingAssignments() throws Exception {
        Enseignant teacher = teachers.saveAndFlush(new Enseignant(unique(), "initial-password-123",
                "before@example.test", "Old", "Teacher", Role.ENSEIGNANT, "MCA", Matier.ALGO));
        String hash = teacher.getPassword();
        String updated = unique();
        mvc.perform(post("/enseignant/saveEditedEnseignant").with(csrf())
                .param("id", teacher.getId().toString())
                .param("username", updated).param("nom", "New").param("prenom", "Teacher")
                .param("email", "new@example.test").param("grade", "professeur")
                .param("specialite", "EDL")
                .param("password", "hacked-password").param("typeRole", "ADMIN"))
                .andExpect(status().is3xxRedirection());
        teachers.flush();
        Enseignant saved = teachers.findEnseignantOptionalById(teacher.getId()).orElseThrow();
        assertEquals(updated, saved.getUsername());
        assertEquals("New", saved.getNom());
        assertEquals("professeur", saved.getGrade());
        assertEquals(Matier.EDL, saved.getSpecialite());
        assertEquals(hash, saved.getPassword());
        assertEquals(Role.ENSEIGNANT, saved.getTypeRole());
    }

    @Test
    void cfdEditPreservesCredentialsRoleAndLogDate() throws Exception {
        Date logDate = new Date(1700000000000L);
        CFD cfd = cfds.saveAndFlush(new CFD(unique(), "initial-password-123",
                "before@example.test", "Old", "CFD", Role.CFD, logDate));
        String hash = cfd.getPassword();
        mvc.perform(post("/cfd/saveEditedCFD").with(csrf())
                .param("id", cfd.getId().toString()).param("username", cfd.getUsername())
                .param("nom", "Updated").param("prenom", "CFD").param("email", "new@example.test")
                .param("password", "hacked-password").param("typeRole", "CANDIDAT"))
                .andExpect(status().is3xxRedirection());
        cfds.flush();
        CFD saved = (CFD) accounts.findById(cfd.getId()).orElseThrow();
        assertEquals("Updated", saved.getNom());
        assertEquals(hash, saved.getPassword());
        assertEquals(Role.CFD, saved.getTypeRole());
        assertEquals(logDate, saved.getLogDate());
    }

    @Test
    void viceDeanEditPreservesCredentialsAndAssociatedCompetition() throws Exception {
        Date logDate = new Date(1700000000000L);
        VD vd = viceDeans.saveAndFlush(new VD(unique(), "initial-password-123",
                "before@example.test", "Old", "ViceDean", Role.VD, logDate));
        InfoConcour comp = new InfoConcour(1, "Speciality", Matier.ALGO, Matier.EDL, new Date());
        comp.setVd(vd);
        competitions.saveAndFlush(comp);
        String hash = vd.getPassword();
        mvc.perform(post("/vd/saveEditedVD").with(csrf())
                .param("id", vd.getId().toString()).param("username", vd.getUsername())
                .param("nom", "Updated").param("prenom", "VD").param("email", "new@example.test")
                .param("password", "hacked-password").param("typeRole", "ADMIN"))
                .andExpect(status().is3xxRedirection());
        viceDeans.flush();
        VD saved = (VD) accounts.findById(vd.getId()).orElseThrow();
        assertEquals("Updated", saved.getNom());
        assertEquals(hash, saved.getPassword());
        assertEquals(Role.VD, saved.getTypeRole());
        assertEquals(logDate, saved.getLogDate());
        assertEquals(vd.getId(), competitions.findById(comp.getId()).orElseThrow().getVd().getId());
    }

    @Test
    void crossRoleOrUnknownIdsCannotBeEdited() throws Exception {
        CFD cfd = cfds.saveAndFlush(new CFD(unique(), "initial-password-123",
                "before@example.test", "Old", "CFD", Role.CFD, new Date()));
        mvc.perform(get("/enseignant/editEnseignant").param("id", cfd.getId().toString()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/enseignant/saveEditedEnseignant").with(csrf())
                .param("id", cfd.getId().toString()).param("username", unique())
                .param("nom", "Changed").param("prenom", "User")
                .param("email", "x@example.test").param("grade", "MCA").param("specialite", "ALGO"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/vd/saveEditedVD").with(csrf())
                .param("id", Long.toString(Long.MAX_VALUE)).param("username", unique())
                .param("nom", "Changed").param("prenom", "User").param("email", "x@example.test"))
                .andExpect(status().isNotFound());
        assertEquals("Old", cfds.findById(cfd.getId()).orElseThrow().getNom());
    }

    @Test
    void duplicateUsernameAcrossRolesIsRejectedWithoutSavingChanges() throws Exception {
        CFD cfd = cfds.saveAndFlush(new CFD(unique(), "initial-password-123",
                "before@example.test", "Old", "CFD", Role.CFD, new Date()));
        VD vd = viceDeans.saveAndFlush(new VD(unique(), "initial-password-123",
                "before@example.test", "Other", "VD", Role.VD, new Date()));
        mvc.perform(post("/cfd/saveEditedCFD").with(csrf())
                .param("id", cfd.getId().toString()).param("username", vd.getUsername())
                .param("nom", "ShouldNotPersist").param("prenom", "CFD")
                .param("email", "new@example.test"))
                .andExpect(status().isOk())
                .andExpect(view().name("AdminCFDEdit"))
                .andExpect(model().attributeHasFieldErrors("cfd", "username"));
        cfds.flush();
        assertEquals("Old", cfds.findById(cfd.getId()).orElseThrow().getNom());
    }

    @Test
    @WithMockUser(username = "non-admin", roles = "CANDIDAT")
    void nonAdminCannotEditStaffAccounts() throws Exception {
        mvc.perform(post("/cfd/saveEditedCFD").with(csrf())
                .param("id", "1")).andExpect(status().isForbidden());
        mvc.perform(post("/vd/saveEditedVD").with(csrf())
                .param("id", "1")).andExpect(status().isForbidden());
        mvc.perform(post("/enseignant/saveEditedEnseignant").with(csrf())
                .param("id", "1")).andExpect(status().isForbidden());
    }

    private String unique() { return "staff-" + UUID.randomUUID(); }
}
