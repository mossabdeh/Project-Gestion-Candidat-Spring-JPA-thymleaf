package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.sec.DevelopmentAdminInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DevelopmentAdminInitializerTests {

    private final AdminRepo adminRepo = mock(AdminRepo.class);

    @Test
    void runsOnlyInDevProfile() {
        Profile profile = DevelopmentAdminInitializer.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());
    }

    @Test
    void missingCredentialsNeverCreateAdmin() {
        new DevelopmentAdminInitializer(adminRepo, "", "").run();
        new DevelopmentAdminInitializer(adminRepo, "developer", "").run();
        new DevelopmentAdminInitializer(adminRepo, "", "strong-password-123").run();

        verifyNoInteractions(adminRepo);
    }

    @Test
    void weakPasswordIsRejected() {
        assertThrows(IllegalStateException.class,
                () -> new DevelopmentAdminInitializer(adminRepo, "developer", "short").run());
        assertThrows(IllegalStateException.class,
                () -> new DevelopmentAdminInitializer(adminRepo, "same-password", "same-password").run());

        verifyNoInteractions(adminRepo);
    }

    @Test
    void existingAdminIsNotOverwritten() {
        when(adminRepo.findByUsername("developer")).thenReturn(Optional.of(new Admin()));

        new DevelopmentAdminInitializer(adminRepo, "developer", "strong-password-123").run();

        verify(adminRepo, never()).save(any());
    }

    @Test
    void createsAdminWithProvidedCredentials() {
        when(adminRepo.findByUsername("developer")).thenReturn(Optional.empty());

        new DevelopmentAdminInitializer(adminRepo, "developer", "strong-password-123").run();

        org.mockito.ArgumentCaptor<Admin> captor = org.mockito.ArgumentCaptor.forClass(Admin.class);
        verify(adminRepo).save(captor.capture());
        Admin admin = captor.getValue();
        assertEquals("developer", admin.getUsername());
        assertEquals("strong-password-123", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getTypeRole());
    }
}
