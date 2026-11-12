package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.sec.DevelopmentRoleSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DevelopmentRoleSeederTests {
    private final UserAccountRepo users = mock(UserAccountRepo.class);
    private final AdminRepo admins = mock(AdminRepo.class);
    private final CandidatRepo candidates = mock(CandidatRepo.class);
    private final EnseignantRepo teachers = mock(EnseignantRepo.class);
    private final CFDRepo cfds = mock(CFDRepo.class);
    private final VDRepo viceDeans = mock(VDRepo.class);

    private DevelopmentRoleSeeder seeder(String password) {
        return new DevelopmentRoleSeeder(users, admins, candidates, teachers, cfds, viceDeans, password);
    }

    @Test
    void isRestrictedToExplicitOptInAndDevProfile() {
        Profile profile = DevelopmentRoleSeeder.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());

        ConditionalOnProperty optIn = DevelopmentRoleSeeder.class.getAnnotation(ConditionalOnProperty.class);
        assertNotNull(optIn);
        assertArrayEquals(new String[]{"dev.seed.enabled"}, optIn.name());
        assertEquals("true", optIn.havingValue());
        assertFalse(optIn.matchIfMissing());
    }

    @Test
    void missingOrWeakPasswordFailsBeforeTouchingAnyRepository() {
        assertThrows(IllegalStateException.class, () -> seeder("").run());
        assertThrows(IllegalStateException.class, () -> seeder("short").run());
        verifyNoInteractions(users, admins, candidates, teachers, cfds, viceDeans);
    }

    @Test
    void createsExactlyOneAccountForEachRole() {
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());

        seeder("dev-test-password-12345").run();

        var a = org.mockito.ArgumentCaptor.forClass(Admin.class);
        var c = org.mockito.ArgumentCaptor.forClass(Candidat.class);
        var e = org.mockito.ArgumentCaptor.forClass(Enseignant.class);
        var f = org.mockito.ArgumentCaptor.forClass(CFD.class);
        var v = org.mockito.ArgumentCaptor.forClass(VD.class);
        verify(admins).save(a.capture());
        verify(candidates).save(c.capture());
        verify(teachers).save(e.capture());
        verify(cfds).save(f.capture());
        verify(viceDeans).save(v.capture());
        assertEquals("dev-admin", a.getValue().getUsername());
        assertEquals(Role.ADMIN, a.getValue().getTypeRole());
        assertEquals("dev-candidate", c.getValue().getUsername());
        assertEquals(Role.CANDIDAT, c.getValue().getTypeRole());
        assertNull(c.getValue().getCode());
        assertEquals("dev-teacher", e.getValue().getUsername());
        assertEquals(Role.ENSEIGNANT, e.getValue().getTypeRole());
        assertEquals(Matier.ALGO, e.getValue().getSpecialite());
        assertEquals("dev-cfd", f.getValue().getUsername());
        assertEquals(Role.CFD, f.getValue().getTypeRole());
        assertEquals("dev-vd", v.getValue().getUsername());
        assertEquals(Role.VD, v.getValue().getTypeRole());
    }

    @Test
    void restartingDoesNotRecreateOrChangeExistingAccounts() {
        when(users.findByUsername(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            User existing = switch (username) {
                case "dev-admin" -> new Admin();
                case "dev-candidate" -> new Candidat();
                case "dev-teacher" -> new Enseignant();
                case "dev-cfd" -> new CFD();
                case "dev-vd" -> new VD();
                default -> throw new IllegalArgumentException(username);
            };
            existing.setUsername(username);
            existing.setTypeRole(switch (username) {
                case "dev-admin" -> Role.ADMIN;
                case "dev-candidate" -> Role.CANDIDAT;
                case "dev-teacher" -> Role.ENSEIGNANT;
                case "dev-cfd" -> Role.CFD;
                default -> Role.VD;
            });
            return Optional.of(existing);
        });

        seeder("dev-test-password-12345").run();

        verifyNoInteractions(admins, candidates, teachers, cfds, viceDeans);
        verify(users, times(5)).findByUsername(anyString());
    }

    @Test
    void refusesToReuseUsernameAssignedToDifferentRole() {
        User existing = new Admin();
        existing.setTypeRole(Role.ADMIN);
        when(users.findByUsername("dev-admin")).thenReturn(Optional.of(existing));
        when(users.findByUsername("dev-candidate")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> seeder("dev-test-password-12345").run());
        verifyNoInteractions(admins, candidates, teachers, cfds, viceDeans);
    }
}
